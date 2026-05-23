package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.toUiErrorStringRes
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pair
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.FindPairByInviteKeyUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.RequestJoinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface JoinPairUiState {
    object Idle : JoinPairUiState
    object Searching : JoinPairUiState
    object SendingRequest : JoinPairUiState
    data class WaitingForApproval(
        val pairName: String,
        val hostId: String
    ) : JoinPairUiState
    data class Joined(
        val pairId: String,
        val petId: String
    ) : JoinPairUiState
    data class Error(val messageResId: Int) : JoinPairUiState
}

@HiltViewModel
class JoinPairViewModel @Inject constructor(
    private val findPairByInviteKeyUseCase: FindPairByInviteKeyUseCase,
    private val requestJoinUseCase: RequestJoinUseCase,
    private val pairRepository: PairRepository,
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<JoinPairUiState>(JoinPairUiState.Idle)
    val uiState: StateFlow<JoinPairUiState> = _uiState.asStateFlow()

    private var currentPairId: String? = null
    private var pairObservationJob: Job? = null

    fun submitInviteCode(inviteCode: String) {
        viewModelScope.launch {
            if (inviteCode.isBlank()) {
                _uiState.value = JoinPairUiState.Error(R.string.error_pair_invalid_input)
                return@launch
            }

            _uiState.value = JoinPairUiState.Searching

            // 1. Ищем пару по коду
            when (val pairResult = findPairByInviteKeyUseCase(inviteCode.uppercase())) {
                is DomainResult.Success -> {
                    val pair = pairResult.data

                    // 2. Отправляем запрос на вступление
                    _uiState.value = JoinPairUiState.SendingRequest

                    val guestId = userRepository.getCurrentUserId()
                        ?: run {
                            _uiState.value = JoinPairUiState.Error(R.string.error_not_authenticated)
                            return@launch
                        }

                    when (val joinResult = requestJoinUseCase(pair.id)) {
                        is DomainResult.Success -> {
                            currentPairId = pair.id
                            _uiState.value = JoinPairUiState.WaitingForApproval(
                                pairName = pair.name,
                                hostId = pair.userId1
                            )
                            startObservingPair(pair.id)
                        }
                        is DomainResult.Failure -> {
                            _uiState.value = JoinPairUiState.Error(joinResult.error.toUiErrorStringRes())
                        }
                    }
                }
                is DomainResult.Failure -> {
                    _uiState.value = JoinPairUiState.Error(pairResult.error.toUiErrorStringRes())
                }
            }
        }
    }

    private fun startObservingPair(pairId: String) {
        pairObservationJob?.cancel()
        pairObservationJob = viewModelScope.launch {
            pairRepository.observePair(pairId).collect { pair ->
                if (pair != null) {
                    handlePairUpdate(pair)
                }
            }
        }
    }

    private fun handlePairUpdate(pair: Pair) {
        // Хост принял запрос → userId2 заполнен
        if (pair.userId2 != null && pair.userId2 == userRepository.getCurrentUserId()) {
            viewModelScope.launch {
                sessionRepository.saveActivePairId(pair.id)
                sessionRepository.saveActivePetId(pair.currentPetId)
                sessionRepository.savePairStatus(pair.status.name)
                _uiState.value = JoinPairUiState.Joined(
                    pairId = pair.id,
                    petId = pair.currentPetId
                )
                pairObservationJob?.cancel()
            }
        }
        // Хост отклонил запрос → pendingRequest пуст, но userId2 всё ещё null
        else if (pair.pendingRequest == null && pair.userId2 == null) {
            _uiState.value = JoinPairUiState.Error(R.string.error_pair_request_rejected)
            pairObservationJob?.cancel()
        }
        // Хост выгнал нас (если уже были приняты)
        else if (pair.status.name == PairStatus.PENDING.name && pair.userId2 == null) {
            _uiState.value = JoinPairUiState.Error(R.string.error_pair_kicked)
            pairObservationJob?.cancel()
        }
    }

    fun leaveSession() {
        val pairId = currentPairId ?: return
        val guestId = userRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            when (val result = pairRepository.leaveSession(pairId, guestId)) {
                is DomainResult.Success -> {
                    sessionRepository.clearAllSessionData()
                    resetToIdle()
                }
                is DomainResult.Failure -> {
                    _uiState.value = JoinPairUiState.Error(result.error.toUiErrorStringRes())
                }
            }
        }
    }

    fun resetToIdle() {
        pairObservationJob?.cancel()
        currentPairId = null
        _uiState.value = JoinPairUiState.Idle
    }

    fun resetState() {
        pairObservationJob?.cancel()
        currentPairId = null
        _uiState.value = JoinPairUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        pairObservationJob?.cancel()
    }
}