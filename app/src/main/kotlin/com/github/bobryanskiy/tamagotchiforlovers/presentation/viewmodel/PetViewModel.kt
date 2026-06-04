package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.di.IoDispatcher
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.ApplyPetActionUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CalculateLiveStatsUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CheckPetLifeDecayUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.MathTaskGeneratorUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import com.github.bobryanskiy.tamagotchiforlovers.presentation.coordinator.PetAccessController
import com.github.bobryanskiy.tamagotchiforlovers.presentation.coordinator.PetNotificationsCoordinator
import com.github.bobryanskiy.tamagotchiforlovers.presentation.mapper.toUiErrorStringRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
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
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

sealed class PetUiState {
    data object Loading : PetUiState()
    data class Content(val pet: Pet) : PetUiState()
    data class GameOver(val pet: Pet) : PetUiState()
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
    private val userRepository: UserRepository,
    private val applyActionUseCase: ApplyPetActionUseCase,
    private val checkPetLifeDecayUseCase: CheckPetLifeDecayUseCase,
    private val accessController: PetAccessController,
    private val notificationsCoordinator: PetNotificationsCoordinator,
    private val calculateLiveStatsUseCase: CalculateLiveStatsUseCase,
    private val clock: Clock,
    val taskGenerator: MathTaskGeneratorUseCase,
    private val logger: AppLogger,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
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

    private var notificationsEnabled: Boolean = true
    private var activePetJob: Job? = null

    companion object {
        private const val TAG = "PetVM"
        private const val LIFE_STATE_CHECK_INTERVAL = 30_000L
        private const val UI_UPDATE_INTERVAL = 10_000L
        private const val POST_ACTION_DELAY = 500L
    }

    /**
     * Точка входа для наблюдений. Вызывается из UI через LaunchedEffect.
     * Это позволяет тестировать ViewModel без запуска side effects.
     */
    fun start() {
        if (activePetJob != null) return

        activePetJob = viewModelScope.launch(ioDispatcher) {
            supervisorScope {
                launch {
                    try { observePet() }
                    catch (e: CancellationException) { throw e }
                    catch (e: Exception) { logger.e(TAG, "observePet failed", e) }
                }
                launch {
                    try { observePairState() }
                    catch (e: CancellationException) { throw e }
                    catch (e: Exception) { logger.e(TAG, "observePairState failed", e) }
                }
                launch {
                    try { verifyPetOwnership() }
                    catch (e: CancellationException) { throw e }
                    catch (e: Exception) { logger.e(TAG, "verifyPetOwnership failed", e) }
                }
                launch {
                    try { observeNotificationsSetting() }
                    catch (e: CancellationException) { throw e }
                    catch (e: Exception) { logger.e(TAG, "observeNotificationsSetting failed", e) }
                }
                launch {
                    try { startLifeStateDecayChecker() }
                    catch (e: CancellationException) { throw e }
                    catch (e: Exception) { logger.e(TAG, "startLifeStateDecayChecker failed", e) }
                }
            }
        }
    }

    private suspend fun verifyPetOwnership() {
        petRepository.observePet(petId)
            .take(1)
            .collect { pet ->
                pet?.let { accessController.ensureAccess(it) }
            }
    }

    private suspend fun observeNotificationsSetting() {
        notificationsCoordinator.observeNotificationsEnabled().collect { enabled ->
            notificationsEnabled = enabled
            logger.d(TAG, "🔔 Notifications setting changed: $enabled")

            if (!enabled) {
                notificationsCoordinator.cancelAlarm(petId)
            } else {
                (_uiState.value as? PetUiState.Content)?.pet?.let {
                    notificationsCoordinator.scheduleAlarm(it, notificationsEnabled)
                }
            }
        }
    }

    private suspend fun observePet() {
        petRepository.observePet(petId)
            .catch { e -> logger.e(TAG, "observePet error", e) }
            .collect { pet ->
                if (pet == null) {
                    _uiState.value = PetUiState.Loading
                    return@collect
                }

                val decayResult = checkPetLifeDecayUseCase(pet)
                val actualPet = decayResult.pet

                if (decayResult.hasChanged) {
                    logger.d(TAG, "🔄 State changed on load: ${pet.lifeState.status} → ${decayResult.newState.status}")
                    petRepository.updateCriticalState(petId, decayResult.newState)

                    if (decayResult.newState.isTerminal() && !pet.lifeState.isTerminal()) {
                        handlePetDeath(actualPet)
                    }
                }

                _uiState.value = when {
                    actualPet.lifeState.isTerminal() -> {
                        if (actualPet.profile.currentPairId != null) {
                            handlePetDeath(actualPet)
                        }
                        PetUiState.GameOver(actualPet)
                    }
                    else -> PetUiState.Content(actualPet)
                }

                if (!actualPet.lifeState.isTerminal()) {
                    notificationsCoordinator.scheduleAlarm(actualPet, notificationsEnabled)
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observePairState() {
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
                    (_uiState.value as? PetUiState.Content)?.pet?.let {
                        accessController.ensureAccess(it)
                    }
                }
            }
    }

    fun onActionRequested(action: PetAction) {
        _dialogState.value = TaskDialogState(action = action, isProcessing = false)
    }

    fun onTaskCompleted() {
        val currentDialog = _dialogState.value ?: return
        viewModelScope.launch(ioDispatcher) {
            _dialogState.value = currentDialog.copy(isProcessing = true)
            when (val result = applyActionUseCase(petId, currentDialog.action)) {
                is DomainResult.Success -> {
                    _dialogState.value = null
                    logger.d(TAG, "✅ Action completed, rescheduling alarm")
                    delay(POST_ACTION_DELAY.milliseconds)
                    (_uiState.value as? PetUiState.Content)?.pet?.let {
                        notificationsCoordinator.scheduleAlarm(it, notificationsEnabled)
                    }
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
            val result = withContext(ioDispatcher) {
                petRepository.deletePet(petId)
            }
            when (result) {
                is DomainResult.Success -> onDeleted()
                is DomainResult.Failure -> _uiEvent.emit(UiEvent.ShowError(R.string.error_unknown))
            }
        }
    }

    fun onDialogDismiss() {
        _dialogState.value = null
    }

    fun triggerAlarmManually() {
        viewModelScope.launch(ioDispatcher) {
            val pet = (_uiState.value as? PetUiState.Content)?.pet
            if (pet == null) {
                logger.w(TAG, "⚠️ Cannot trigger: pet not loaded")
                return@launch
            }
            notificationsCoordinator.showTestNotification(pet)
            notificationsCoordinator.triggerManualCheck()
        }
    }

    private suspend fun startLifeStateDecayChecker() {
        while (currentCoroutineContext().isActive) {
            val pet = (_uiState.value as? PetUiState.Content)?.pet
            if (pet != null) {
                try {
                    val result = checkPetLifeDecayUseCase(pet)
                    if (result.hasChanged) {
                        logger.d(TAG, "🔄 LifeState changed: ${pet.lifeState.status} → ${result.newState.status}")
                        petRepository.updateCriticalState(petId, result.newState)
                        if (result.newState.isTerminal()) {
                            handlePetDeath(result.pet)
                        } else {
                            notificationsCoordinator.scheduleAlarm(result.pet, notificationsEnabled)
                        }
                    }
                } catch (e: Exception) {
                    logger.e(TAG, "❌ Decay check failed", e)
                }
            }

            delay(LIFE_STATE_CHECK_INTERVAL.milliseconds)
        }
    }

    private suspend fun handlePetDeath(pet: Pet) {
        logger.d(TAG, "🪦 Pet ${pet.id} died (${pet.lifeState.deathCause}), cleaning up pair")

        val pairId = pet.profile.currentPairId
        val userId = pet.profile.ownerUserId

        if (pairId != null && userId != null) {
            try {
                pairRepository.endSession(pairId, userId)
                logger.d(TAG, "✅ Pair $pairId marked as ENDED")

                petRepository.updatePairId(pet.id, null)

                userRepository.updateUserSession(userId, pet.id, null)

                sessionRepository.clearActivePairId()
            } catch (e: Exception) {
                logger.e(TAG, "⚠️ Failed to cleanup pair after pet death", e)
            }
        }

        notificationsCoordinator.cancelAlarm(pet.id)
    }

    override fun onCleared() {
        super.onCleared()
        activePetJob?.cancel()
    }
}
