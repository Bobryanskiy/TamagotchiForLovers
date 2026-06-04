package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.core.util.updateState
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PendingRequest
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.AcceptJoinRequestUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CreatePairWithInviteUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.EndSessionUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.KickPartnerUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.ObservePendingRequestsUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.RejectJoinRequestUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import com.github.bobryanskiy.tamagotchiforlovers.presentation.mapper.toUiErrorStringRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed interface HostPairUiState {
    data object Idle : HostPairUiState
    data class Waiting(
        val pairId: String,
        val inviteCode: String,
        val expiresAt: Long,
        val pendingRequests: List<PendingRequest>
    ) : HostPairUiState
    data class Connected(
        val pairId: String,
        val pairName: String,
        val partnerId: String
    ) : HostPairUiState
    data class Error(val messageResId: Int) : HostPairUiState
}

@HiltViewModel
class HostPairViewModel @Inject constructor(
    private val createPairUseCase: CreatePairWithInviteUseCase,
    private val acceptRequestUseCase: AcceptJoinRequestUseCase,
    private val rejectRequestUseCase: RejectJoinRequestUseCase,
    private val observeRequestsUseCase: ObservePendingRequestsUseCase,
    private val endSessionUseCase: EndSessionUseCase,
    private val kickPartnerUseCase: KickPartnerUseCase,
    private val pairRepository: PairRepository,
    private val sessionRepository: SessionRepository,
    private val petRepository: PetRepository,
    private val userRepository: UserRepository,
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow<HostPairUiState>(HostPairUiState.Idle)
    val uiState: StateFlow<HostPairUiState> = _uiState.asStateFlow()

    private var currentPairId: String? = null
    private var currentCreatorId: String? = null
    private var pairJob: Job? = null
    private var requestsJob: Job? = null

    companion object {
        private const val TAG = "HostPairVM"
        private const val INVITE_TTL_MS = 5 * 60 * 1_000L
    }

    fun initScreen(petId: String?) {
        viewModelScope.launch {
            var savedPairId = sessionRepository.getActivePairId()

            if (savedPairId == null && petId != null) {
                val pet = petRepository.getPetById(petId)
                savedPairId = pet.getOrNull()?.profile?.currentPairId
                if (savedPairId != null) sessionRepository.saveActivePairId(savedPairId)
            }

            if (savedPairId != null) {
                currentPairId = savedPairId
                currentCreatorId = userRepository.getCurrentUserId()
                subscribeToPair(savedPairId)
            } else {
                _uiState.value = HostPairUiState.Idle
            }
        }
    }

    private fun subscribeToPair(pairId: String) {
        pairJob?.cancel()
        pairJob = viewModelScope.launch {
            pairRepository.observePair(pairId)
                .distinctUntilChanged()
                .catch { e -> Timber.tag(TAG).e(e, "observePair error") }
                .collect { pair ->
                    if (pair == null && currentPairId != null) {
                        Timber.tag(TAG).w("⚠️ Ignoring null emission")
                        return@collect
                    }
                    handlePairUpdate(pair)
                }
        }
    }

    private fun handlePairUpdate(
        petPair: com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetPair?
    ) {
        if (petPair == null) {
            _uiState.value = HostPairUiState.Idle
            return
        }

        val isCreator = petPair.userId1 == userRepository.getCurrentUserId()

        // Гость в активной паре — ему нечего делать на HostPairScreen
        if (!isCreator && petPair.status.name == PairStatus.ACTIVE.name) {
            return
        }

        when (petPair.status.name) {
            PairStatus.PENDING.name -> {
                val key = petPair.inviteKey ?: run {
                    Timber.tag(TAG).w("⏳ Key not loaded yet, waiting...")
                    return
                }
                _uiState.value = HostPairUiState.Waiting(
                    pairId = petPair.id,
                    inviteCode = key.code,
                    expiresAt = key.expiresAt,
                    pendingRequests = emptyList()
                )
                startObservingRequests(petPair.id)
            }
            PairStatus.ACTIVE.name -> {
                _uiState.value = HostPairUiState.Connected(
                    pairId = petPair.id,
                    pairName = petPair.name,
                    partnerId = petPair.userId2.orEmpty()
                )
                // Останавливаем наблюдение за requests — пара активна
                requestsJob?.cancel()
            }
            else -> resetToIdle()
        }
    }

    fun createPair(pairName: String, petId: String) {
        viewModelScope.launch {
            val creatorId = userRepository.getCurrentUserId() ?: run {
                _uiState.value = HostPairUiState.Error(R.string.error_not_authenticated)
                return@launch
            }

            when (val result = createPairUseCase(pairName, petId)) {
                is DomainResult.Success -> {
                    val pairId = result.data.pairId
                    currentPairId = pairId
                    currentCreatorId = creatorId

                    sessionRepository.savePairStatus(PairStatus.PENDING.name)
                    sessionRepository.saveActivePairId(pairId)
                    sessionRepository.saveActivePetId(petId)
                    userRepository.updateUserSession(creatorId, petId, pairId)

                    _uiState.value = HostPairUiState.Waiting(
                        pairId = pairId,
                        inviteCode = result.data.inviteCode,
                        expiresAt = result.data.expiresAt,
                        pendingRequests = emptyList()
                    )
                    startObservingRequests(pairId)
                    subscribeToPair(pairId)
                }
                is DomainResult.Failure -> {
                    _uiState.value = HostPairUiState.Error(result.error.toUiErrorStringRes())
                }
            }
        }
    }

    private fun startObservingRequests(pairId: String) {
        requestsJob?.cancel()
        requestsJob = viewModelScope.launch {
            observeRequestsUseCase(pairId)
                .distinctUntilChanged()
                .catch { e -> Timber.tag(TAG).e(e, "observeRequests error") }
                .collect { requests ->
                    _uiState.updateState { state ->
                        if (state is HostPairUiState.Waiting) state.copy(pendingRequests = requests)
                        else state
                    }
                }
        }
    }

    fun acceptRequest(guestId: String) {
        val pairId = currentPairId ?: return
        val callerId = currentCreatorId ?: return

        viewModelScope.launch {
            when (val result = acceptRequestUseCase(pairId, guestId, callerId)) {
                is DomainResult.Success -> { /* Firestore push → handlePairUpdate обновит UI */ }
                is DomainResult.Failure -> _uiState.value = HostPairUiState.Error(result.error.toUiErrorStringRes())
            }
        }
    }

    fun rejectRequest(guestId: String) {
        val pairId = currentPairId ?: return
        val callerId = currentCreatorId ?: return

        viewModelScope.launch {
            when (val result = rejectRequestUseCase(pairId, guestId, callerId)) {
                is DomainResult.Success -> { /* Firestore сам обновит requests */ }
                is DomainResult.Failure -> _uiState.value = HostPairUiState.Error(result.error.toUiErrorStringRes())
            }
        }
    }

    fun regenerateInviteCode() {
        val pairId = currentPairId ?: return
        viewModelScope.launch {
            when (val result = pairRepository.generateInviteKey(pairId)) {
                is DomainResult.Success -> {
                    _uiState.updateState { state ->
                        if (state is HostPairUiState.Waiting) {
                            state.copy(
                                inviteCode = result.data,
                                expiresAt = clock.currentTimeMillis() + INVITE_TTL_MS
                            )
                        } else state
                    }
                }
                is DomainResult.Failure -> _uiState.value = HostPairUiState.Error(result.error.toUiErrorStringRes())
            }
        }
    }

    fun kickPartner() {
        val pairId = currentPairId ?: return
        val callerId = currentCreatorId ?: return

        viewModelScope.launch {
            when (val result = kickPartnerUseCase(pairId, callerId)) {
                is DomainResult.Success -> resetToIdle()
                is DomainResult.Failure -> _uiState.value = HostPairUiState.Error(result.error.toUiErrorStringRes())
            }
        }
    }

    fun endSession() {
        val pairId = currentPairId ?: return
        val callerId = currentCreatorId ?: return

        viewModelScope.launch {
            when (val result = endSessionUseCase(pairId, callerId)) {
                is DomainResult.Success -> {
                    sessionRepository.clearActivePairId()
                    sessionRepository.clearPairStatus()
                    resetToIdle()
                }
                is DomainResult.Failure -> _uiState.value = HostPairUiState.Error(result.error.toUiErrorStringRes())
            }
        }
    }

    fun renamePair(newName: String) {
        val pairId = currentPairId ?: return
        if (newName.isBlank()) return
        viewModelScope.launch {
            when (val result = pairRepository.updatePairName(pairId, newName)) {
                is DomainResult.Success -> {
                    _uiState.updateState {
                        if (it is HostPairUiState.Connected) it.copy(pairName = newName) else it
                    }
                }
                is DomainResult.Failure -> _uiState.value = HostPairUiState.Error(result.error.toUiErrorStringRes())
            }
        }
    }

    fun resetToIdle() {
        pairJob?.cancel(); pairJob = null
        requestsJob?.cancel(); requestsJob = null
        currentPairId = null
        currentCreatorId = null
        _uiState.value = HostPairUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        pairJob?.cancel()
        requestsJob?.cancel()
    }
}
