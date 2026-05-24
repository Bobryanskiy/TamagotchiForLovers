package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.core.notification.NotificationHelper
import com.github.bobryanskiy.tamagotchiforlovers.core.notification.NotificationStringResolver
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.ApplyPetActionUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CalculateTimeDecayUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.MathTaskGeneratorUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.PreparePetNotificationUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import com.github.bobryanskiy.tamagotchiforlovers.presentation.mapper.toUiErrorStringRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PetUiState {
    data object Loading : PetUiState()
    data class Content(val pet: Pet) : PetUiState()
    data object GameOver : PetUiState()
}

sealed interface UiEvent {
    data class ShowError(val messageResId: Int) : UiEvent
}

data class TaskDialogState(
    val action: PetAction,
    val isProcessing: Boolean = false
)

sealed class PairButtonState {
    data object Loading : PairButtonState()
    data object NoPair : PairButtonState()
    data class WaitingApproval(val pairId: String, val hasPendingRequests: Boolean) : PairButtonState()
    data class Active(val pairId: String, val partnerId: String?) : PairButtonState()
}

@HiltViewModel
class PetViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val petRepository: PetRepository,
    private val pairRepository: PairRepository,
    private val sessionRepository: SessionRepository,
    private val applyActionUseCase: ApplyPetActionUseCase,
    private val timeDecayUseCase: CalculateTimeDecayUseCase,
    val taskGenerator: MathTaskGeneratorUseCase,
    private val clock: Clock,
    private val logger: AppLogger
) : ViewModel() {

    private val petId: String = checkNotNull(savedStateHandle["petId"])

    private val _uiState = MutableStateFlow<PetUiState>(PetUiState.Loading)
    val uiState: StateFlow<PetUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private val _dialogState = MutableStateFlow<TaskDialogState?>(null)
    val dialogState: StateFlow<TaskDialogState?> = _dialogState.asStateFlow()

    private val _pairButtonState = MutableStateFlow<PairButtonState>(PairButtonState.Loading)
    val pairButtonState: StateFlow<PairButtonState> = _pairButtonState.asStateFlow()

    private var timeUpdateJob: Job? = null
    private var observeJob: Job? = null
    private var pairObserveJob: Job? = null

    companion object {
        private const val TAG = "PetVM"
    }

    init {
        observePet()
        observePairState()
    }

    private fun observePet() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            petRepository.observePet(petId)
                .catch { e -> logger.e(TAG, "observePet error", e) }
                .collect { pet ->
                    _uiState.value = when {
                        pet == null -> PetUiState.Loading
                        pet.lifeState.isTerminal() -> PetUiState.GameOver
                        else -> PetUiState.Content(pet)
                    }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePairState() {
        pairObserveJob?.cancel()
        pairObserveJob = viewModelScope.launch {
            petRepository.observePet(petId)
                .map { it?.profile?.currentPairId }
                .distinctUntilChanged()
                .flatMapLatest { pairId ->
                    if (pairId == null) {
                        flowOf(PairButtonState.NoPair)
                    } else {
                        pairRepository.observePair(pairId)
                            .catch { e -> logger.e(TAG, "observePair error", e) }
                            .map { pair ->
                                when {
                                    pair == null -> PairButtonState.NoPair
                                    pair.status == PairStatus.ACTIVE ->
                                        PairButtonState.Active(pair.id, pair.userId2)
                                    pair.status == PairStatus.PENDING && pair.userId2 == null ->
                                        PairButtonState.WaitingApproval(
                                            pairId = pair.id,
                                            hasPendingRequests = pair.pendingRequest != null
                                        )
                                    else -> PairButtonState.NoPair
                                }
                            }
                    }
                }
                .collect { state ->
                    logger.d(TAG, "🔗 PairButtonState: $state")
                    _pairButtonState.value = state
                }
        }
    }

    private fun applyAndEmit(pet: Pet) {
        val now = clock.currentTimeMillis()
        val newState = when {
            pet.lifeState.isTerminal() -> PetUiState.GameOver
            else -> PetUiState.Content(timeDecayUseCase(pet, now))
        }
        if (_uiState.value != newState) _uiState.value = newState
    }

    fun onActionRequested(action: PetAction) {
        _dialogState.value = TaskDialogState(action = action, isProcessing = false)
    }

    fun onTaskCompleted() {
        val currentDialog = _dialogState.value ?: return
        viewModelScope.launch {
            _dialogState.value = currentDialog.copy(isProcessing = true)
            when (val result = applyActionUseCase(petId, currentDialog.action)) {
                is DomainResult.Success -> {
                    _dialogState.value = null
                }
                is DomainResult.Failure -> {
                    val resId = result.error.toUiErrorStringRes()
                    _uiEvent.emit(UiEvent.ShowError(resId))
                    _dialogState.value = null
                }
            }
        }
    }

    fun abandonPet(onDeleted: () -> Unit) {
        viewModelScope.launch {
            when (petRepository.deletePet(petId)) {
                is DomainResult.Success -> {
                    sessionRepository.clearActivePetId()
                    onDeleted()
                }
                is DomainResult.Failure -> {
                    _uiEvent.emit(UiEvent.ShowError(R.string.error_unknown))
                }
            }
        }
    }

    fun onDialogDismiss() {
        _dialogState.value = null
    }

    override fun onCleared() {
        super.onCleared()
        timeUpdateJob?.cancel()
        observeJob?.cancel()
        pairObserveJob?.cancel()
    }

    @Inject lateinit var prepareNotification: PreparePetNotificationUseCase
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var stringResolver: NotificationStringResolver

    fun testNotification() {
        viewModelScope.launch {
            // Берём пета из текущего state через safe cast
            val currentState = _uiState.value
            val pet = (currentState as? PetUiState.Content)?.pet ?: run {
                logger.w(TAG, "🔔 Cannot send test notification: pet not loaded yet")
                return@launch
            }

            // Искусственно делаем пета "голодным" для теста
            val testPet = pet.copy(
                stats = pet.stats.copy(
                    hunger = 5,
                    updatedAt = clock.currentTimeMillis()
                )
            )

            val notification = prepareNotification(testPet)
            val title = stringResolver.resolveTitle(notification)
            val message = stringResolver.resolveMessage(notification)

            notificationHelper.showPetNotification(
                NotificationHelper.NotificationData(
                    petId = notification.petId,
                    petName = notification.petName,
                    title = title,
                    message = message,
                    isUrgent = true,
                    status = notification.status
                )
            )
            logger.d(TAG, "🔔 Test notification sent for ${pet.profile.name}")
        }
    }
}