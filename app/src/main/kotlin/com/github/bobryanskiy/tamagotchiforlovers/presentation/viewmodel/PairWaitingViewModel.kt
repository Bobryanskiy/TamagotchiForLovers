package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.core.util.updateState
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PendingRequest
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.AcceptJoinRequestUseCase
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
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PairWaitingUiState {
    data object Loading : PairWaitingUiState
    data class Waiting(
        val pairId: String,
        val inviteCode: String,
        val expiresAt: Long,
        val pendingRequests: List<PendingRequest>
    ) : PairWaitingUiState
    data object Activated : PairWaitingUiState
    data class Error(@param:StringRes val messageResId: Int) : PairWaitingUiState
}

@HiltViewModel
class PairWaitingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pairRepository: PairRepository,
    private val userRepository: UserRepository,
    private val observeRequestsUseCase: ObservePendingRequestsUseCase,
    private val acceptRequestUseCase: AcceptJoinRequestUseCase,
    private val rejectRequestUseCase: RejectJoinRequestUseCase,
    private val clock: Clock
) : ViewModel() {

    private val pairId: String = checkNotNull(savedStateHandle["pairId"])

    private val _uiState = MutableStateFlow<PairWaitingUiState>(PairWaitingUiState.Loading)
    val uiState: StateFlow<PairWaitingUiState> = _uiState.asStateFlow()

    private val _nicknames = MutableStateFlow<Map<String, String?>>(emptyMap())
    val nicknames: StateFlow<Map<String, String?>> = _nicknames.asStateFlow()

    private var pairJob: Job? = null
    private var requestsJob: Job? = null

    init {
        observePair()
        observeRequests()
    }

    private fun observePair() {
        pairJob = viewModelScope.launch {
            pairRepository.observePair(pairId)
                .catch { /* ignore */ }
                .collect { pair ->
                    when {
                        pair == null -> {
                            _uiState.value = PairWaitingUiState.Error(R.string.error_pair_not_found)
                        }
                        pair.status == PairStatus.ACTIVE -> {
                            _uiState.value = PairWaitingUiState.Activated
                        }
                        pair.inviteKey != null && _uiState.value !is PairWaitingUiState.Activated -> {
                            val current = _uiState.value
                            val requests = if (current is PairWaitingUiState.Waiting)
                                current.pendingRequests else emptyList()
                            _uiState.value = PairWaitingUiState.Waiting(
                                pairId = pair.id,
                                inviteCode = pair.inviteKey.code,
                                expiresAt = pair.inviteKey.expiresAt,
                                pendingRequests = requests
                            )
                        }
                    }
                }
        }
    }

    private fun observeRequests() {
        requestsJob = viewModelScope.launch {
            observeRequestsUseCase(pairId)
                .catch { /* ignore */ }
                .collect { requests ->
                    requests.forEach { request ->
                        if (!_nicknames.value.containsKey(request.guestId)) {
                            val nick = userRepository.getUserNickname(request.guestId)
                            _nicknames.updateState { it + (request.guestId to nick) }
                        }
                    }
                    val current = _uiState.value
                    if (current is PairWaitingUiState.Waiting) {
                        _uiState.value = current.copy(pendingRequests = requests)
                    }
                }
        }
    }

    fun regenerateInviteCode() {
        viewModelScope.launch {
            when (val result = pairRepository.generateInviteKey(pairId)) {
                is DomainResult.Success -> {
                    val current = _uiState.value
                    if (current is PairWaitingUiState.Waiting) {
                        _uiState.value = current.copy(
                            inviteCode = result.data,
                            expiresAt = clock.currentTimeMillis() + 5 * 60 * 1000L
                        )
                    }
                }
                is DomainResult.Failure -> {
                    _uiState.value = PairWaitingUiState.Error(result.error.toUiErrorStringRes())
                }
            }
        }
    }

    fun acceptRequest(guestId: String) {
        val callerId = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            when (val result = acceptRequestUseCase(pairId, guestId, callerId)) {
                is DomainResult.Success -> { /* Firestore push активирует пару */ }
                is DomainResult.Failure -> {
                    _uiState.value = PairWaitingUiState.Error(result.error.toUiErrorStringRes())
                }
            }
        }
    }

    fun rejectRequest(guestId: String) {
        val callerId = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            when (val result = rejectRequestUseCase(pairId, guestId, callerId)) {
                is DomainResult.Success -> { /* Firestore сам обновит список */ }
                is DomainResult.Failure -> {
                    _uiState.value = PairWaitingUiState.Error(result.error.toUiErrorStringRes())
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pairJob?.cancel()
        requestsJob?.cancel()
    }
}
