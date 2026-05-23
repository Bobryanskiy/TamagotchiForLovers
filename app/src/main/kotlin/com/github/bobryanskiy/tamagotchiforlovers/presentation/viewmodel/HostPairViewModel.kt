package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.toUiErrorStringRes
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pair
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PendingRequest
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.AcceptJoinRequestUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CreatePairWithInviteUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.KickPartnerUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.ObservePendingRequestsUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.RejectJoinRequestUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HostPairUiState {
    object Idle : HostPairUiState
    data class Waiting(
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
    private val kickPartnerUseCase: KickPartnerUseCase,
    private val pairRepository: PairRepository,
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository,
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow<HostPairUiState>(HostPairUiState.Idle)
    val uiState: StateFlow<HostPairUiState> = _uiState.asStateFlow()

    private var currentPairId: String? = null
    private var currentCreatorId: String? = null
    private var requestsJob: Job? = null

    fun initScreen() {
        viewModelScope.launch {
            val savedPairId = sessionRepository.getActivePairId()
            if (savedPairId != null) {
                currentPairId = savedPairId
                currentCreatorId = userRepository.getCurrentUserId()

                Log.d("FIREBASE_DEBUG", "🔄 [VM] Subscribing to pair from session: $savedPairId")

                pairRepository.observePair(savedPairId)
                    .collect { pair ->
                        Log.d("FIREBASE_DEBUG", "📲 [VM] Update received: status=${pair?.status}, hasKey=${pair?.inviteKey != null}")
                        handlePairUpdate(pair)
                    }
                return@launch
            }

            // Если нет savedPairId, но у пользователя есть активная пара в сессии (через petId)
            // или мы можем проверить через UserRepository текущий статус
            // В этом случае остаемся в Idle, пользователь должен создать новую пару
            _uiState.value = HostPairUiState.Idle
        }
    }

    private fun handlePairUpdate(pair: Pair?) {
        if (pair == null && currentPairId != null) {
            android.util.Log.w("HOST_VM", "⚠️ Ignoring null emission (waiting for Firestore data)")
            return
        }

        if (pair == null) {
            _uiState.value = HostPairUiState.Idle
            return
        }

        android.util.Log.d("HOST_VM", "📊 Mapping state: ${pair.status.name}")

        when (pair.status.name) {
            PairStatus.PENDING.name -> {
                val key = pair.inviteKey
                if (key == null) {
                    android.util.Log.w("HOST_VM", "⏳ Key not loaded yet, waiting...")
                    return
                }

                _uiState.value = HostPairUiState.Waiting(
                    inviteCode = key.code,
                    expiresAt = key.expiresAt,
                    pendingRequests = emptyList()
                )
                startObservingRequests(pair.id)
            }
            PairStatus.ACTIVE.name -> {
                _uiState.value = HostPairUiState.Connected(
                    pairId = pair.id,
                    pairName = pair.name,
                    partnerId = pair.userId2 ?: "Unknown"
                )
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
                    currentPairId = result.data.pairId
                    currentCreatorId = creatorId

                    viewModelScope.launch {
                        sessionRepository.savePairStatus(PairStatus.PENDING.name)
                        sessionRepository.saveActivePairId(result.data.pairId)
                    }

                    _uiState.value = HostPairUiState.Waiting(
                        inviteCode = result.data.inviteCode,
                        expiresAt = result.data.expiresAt,
                        pendingRequests = emptyList()
                    )

                    startObservingRequests(result.data.pairId)
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
            observeRequestsUseCase(pairId).collect { requests ->
                _uiState.update { state ->
                    if (state is HostPairUiState.Waiting) {
                        state.copy(pendingRequests = requests)
                    } else state
                }
            }
        }
    }

    fun acceptRequest(guestId: String) {
        val pairId = currentPairId ?: return
        val callerId = currentCreatorId ?: return

        viewModelScope.launch {
            when (val result = acceptRequestUseCase(pairId, guestId, callerId)) {
                is DomainResult.Success -> {
                    // Firestore обновится -> handlePairUpdate покажет Connected
                }
                is DomainResult.Failure -> {
                    _uiState.value = HostPairUiState.Error(result.error.toUiErrorStringRes())
                }
            }
        }
    }

    fun rejectRequest(guestId: String) {
        val pairId = currentPairId ?: return
        val callerId = currentCreatorId ?: return

        viewModelScope.launch {
            when (val result = rejectRequestUseCase(pairId, guestId, callerId)) {
                is DomainResult.Success -> { /* Firestore обновит список */ }
                is DomainResult.Failure -> {
                    _uiState.value = HostPairUiState.Error(result.error.toUiErrorStringRes())
                }
            }
        }
    }

    fun regenerateInviteCode() {
        val pairId = currentPairId ?: return
        viewModelScope.launch {
            when (val result = pairRepository.generateInviteKey(pairId)) {
                is DomainResult.Success -> {
                    _uiState.update { state ->
                        if (state is HostPairUiState.Waiting) {
                            state.copy(
                                inviteCode = result.data,
                                expiresAt = clock.currentTimeMillis() + (5 * 60 * 1000L)
                            )
                        } else state
                    }
                }
                is DomainResult.Failure -> {
                    _uiState.value = HostPairUiState.Error(result.error.toUiErrorStringRes())
                }
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
            when (val result = pairRepository.endSession(pairId, callerId)) {
                is DomainResult.Success -> {
                    sessionRepository.clearActivePairId()
                    sessionRepository.clearPairStatus()
                    resetToIdle()
                }
                is DomainResult.Failure -> {
                    _uiState.value = HostPairUiState.Error(result.error.toUiErrorStringRes())
                }
            }
        }
    }

    fun renamePair(newName: String) {
        val pairId = currentPairId ?: return
        if (newName.isBlank()) return
        viewModelScope.launch {
            when (val result = pairRepository.updatePairName(pairId, newName)) {
                is DomainResult.Success -> {
                    _uiState.update {
                        if (it is HostPairUiState.Connected) it.copy(pairName = newName) else it
                    }
                }
                is DomainResult.Failure -> _uiState.value = HostPairUiState.Error(result.error.toUiErrorStringRes())
            }
        }
    }

    fun resetToIdle() {
        requestsJob?.cancel()
        currentPairId = null
        currentCreatorId = null
        _uiState.value = HostPairUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        requestsJob?.cancel()
    }
}