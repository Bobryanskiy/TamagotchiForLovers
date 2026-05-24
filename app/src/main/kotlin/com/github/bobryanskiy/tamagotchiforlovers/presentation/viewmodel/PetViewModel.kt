package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.core.alarm.PetAlarmManager
import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.core.notification.NotificationHelper
import com.github.bobryanskiy.tamagotchiforlovers.core.usecase.CheckAndNotifyPetsWorkerUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SettingsRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.ApplyPetActionUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CalculateLiveStatsUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.EvaluatePetCriticalStateUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.MathTaskGeneratorUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import com.github.bobryanskiy.tamagotchiforlovers.presentation.mapper.toUiErrorStringRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.isActive
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
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: SessionRepository,
    private val applyActionUseCase: ApplyPetActionUseCase,
    private val calculateLiveStatsUseCase: CalculateLiveStatsUseCase,
    private val evaluatePetCriticalStateUseCase: EvaluatePetCriticalStateUseCase,
    private val petAlarmManager: PetAlarmManager,
    val taskGenerator: MathTaskGeneratorUseCase,
    private val logger: AppLogger,
    private val clock: Clock,

    private val checkAndNotifyPetsWorkerUseCase: CheckAndNotifyPetsWorkerUseCase,
    private val notificationHelper: NotificationHelper
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


    private var observeJob: Job? = null
    private var pairObserveJob: Job? = null
    private var notificationsJob: Job? = null
    private var notificationsEnabled: Boolean = true

    companion object {
        private const val TAG = "PetVM"
        private const val LIFE_STATE_CHECK_INTERVAL = 30_000L
    }

    init {
        observePet()
        observePairState()
        verifyPetOwnership()
        observeNotificationsSetting()
        startLifeStateDecayChecker()
    }

    private suspend fun verifyAccessAndCleanup(pet: Pet) {
        val currentUserId = userRepository.getCurrentUserId() ?: return

        if (pet.profile.ownerUserId == currentUserId) {
            logger.d(TAG, "✅ User is owner of pet ${pet.id}")
            return
        }

        val pairId = pet.profile.currentPairId
        if (pairId != null) {
            val pairResult = pairRepository.getPair(pairId)
            if (pairResult is DomainResult.Success) {
                val pair = pairResult.data
                if (pair != null &&
                    pair.status == PairStatus.ACTIVE &&
                    (pair.userId1 == currentUserId || pair.userId2 == currentUserId)
                ) {
                    logger.d(TAG, "✅ User is co-owner of pet ${pet.id}")
                    return
                }
            }
        }

        logger.w(TAG, "⚠️ User lost access to pet ${pet.id}, cleaning up")
        cleanupPetAccess()
    }

    private fun verifyPetOwnership() {
        viewModelScope.launch {
            petRepository.observePet(petId)
                .take(1)
                .collect { pet ->
                    pet?.let { verifyAccessAndCleanup(it) }
                }
        }
    }

    private fun observeNotificationsSetting() {
        notificationsJob?.cancel()
        notificationsJob = viewModelScope.launch {
            settingsRepository.observeNotificationsEnabled().collect { enabled ->
                notificationsEnabled = enabled
                logger.d(TAG, "🔔 Notifications setting changed: $enabled")

                if (!enabled) {
                    petAlarmManager.cancelCheck(petId)
                } else {
                    val pet = (_uiState.value as? PetUiState.Content)?.pet
                    pet?.let { scheduleAlarmForPet(it) }
                }
            }
        }
    }

    private fun scheduleAlarmForPet(pet: Pet) {
        petAlarmManager.scheduleSmartAlarm(
            petId = pet.id,
            pet = pet,
            notificationsEnabled = notificationsEnabled
        )
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

                    if (pet != null && !pet.lifeState.isTerminal()) {
                        scheduleAlarmForPet(pet)
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
                    if (pairId == null) flowOf(PairButtonState.NoPair)
                    else pairRepository.observePair(pairId)
                        .catch { e -> logger.e(TAG, "observePair error", e) }
                        .map { pair ->
                            when {
                                pair == null -> PairButtonState.NoPair
                                pair.status == PairStatus.ACTIVE ->
                                    PairButtonState.Active(pair.id, pair.userId2)
                                pair.status == PairStatus.PENDING && pair.userId2 == null ->
                                    PairButtonState.WaitingApproval(pair.id, pair.pendingRequest != null)
                                else -> PairButtonState.NoPair
                            }
                        }
                }
                .collect { state ->
                    _pairButtonState.value = state
                    if (state is PairButtonState.NoPair || state is PairButtonState.WaitingApproval) {
                        checkAndCleanupIfNotOwner()
                    }
                }
        }
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
                    logger.d(TAG, "✅ Action completed, rescheduling alarm")

                    delay(500)
                    val pet = (_uiState.value as? PetUiState.Content)?.pet
                    pet?.let {
                        scheduleAlarmForPet(it)
                    } ?: logger.w(TAG, "⚠️ Pet not loaded, cannot schedule alarm")
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

    fun triggerAlarmManually() {
        viewModelScope.launch {
            val pet = (_uiState.value as? PetUiState.Content)?.pet ?: return@launch

            notificationHelper.showPetNotification(
                NotificationHelper.NotificationData(
                    petId = pet.id,
                    petName = pet.profile.name,
                    title = "🧪 Тест",
                    message = "Если видишь это — уведомления работают!",
                    isUrgent = true,
                    status = pet.lifeState.status
                )
            )
            logger.d(TAG, "🧪 Direct test notification sent")
        }
        viewModelScope.launch {
            val pet = (_uiState.value as? PetUiState.Content)?.pet ?: run {
                logger.w(TAG, "⚠️ Cannot trigger: pet not loaded")
                return@launch
            }

            logger.d(TAG, "🧪 Manually triggering CheckAndNotifyPets for ${pet.id}")

            try {
                checkAndNotifyPetsWorkerUseCase()
                logger.d(TAG, "✅ Manual trigger completed successfully")
            } catch (e: Exception) {
                logger.e(TAG, "❌ Manual trigger failed", e)
            }
        }
    }

    private fun startLifeStateDecayChecker() {
        viewModelScope.launch {
            while (isActive) {
                delay(LIFE_STATE_CHECK_INTERVAL)

                val pet = (_uiState.value as? PetUiState.Content)?.pet ?: continue
                if (pet.lifeState.isTerminal()) continue

                val currentTime = clock.currentTimeMillis()
                val decayedPet = calculateLiveStatsUseCase(pet, currentTime)
                val newState = evaluatePetCriticalStateUseCase(decayedPet.stats, currentTime)

                if (newState.status != pet.lifeState.status) {
                    logger.d(TAG, "🔄 LifeState changed: ${pet.lifeState.status} → ${newState.status}")
                    petRepository.updateCriticalState(petId, newState)
                    scheduleAlarmForPet(decayedPet.copy(lifeState = newState))
                }
            }
        }
    }

    private suspend fun checkAndCleanupIfNotOwner() {
        val pet = (_uiState.value as? PetUiState.Content)?.pet ?: return
        verifyAccessAndCleanup(pet)
    }

    private suspend fun cleanupPetAccess() {
        runCatching { petRepository.deletePet(petId) }
        sessionRepository.clearActivePetId()
        sessionRepository.clearActivePairId()
        sessionRepository.clearPairStatus()
    }

    override fun onCleared() {
        super.onCleared()
        observeJob?.cancel()
        pairObserveJob?.cancel()
        notificationsJob?.cancel()
    }
}