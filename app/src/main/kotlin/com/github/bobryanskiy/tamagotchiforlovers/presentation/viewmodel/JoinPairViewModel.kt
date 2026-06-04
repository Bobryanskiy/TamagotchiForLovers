package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.core.util.updateState
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.FindPairByInviteKeyUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.LeaveSessionUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.RequestJoinUseCase
import com.github.bobryanskiy.tamagotchiforlovers.presentation.mapper.toUiErrorStringRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed interface JoinPairUiState {
    data object Idle : JoinPairUiState
    data object Searching : JoinPairUiState
    data object SendingRequest : JoinPairUiState
    data class WaitingForApproval(val pairName: String, val hostId: String) : JoinPairUiState
    data class Joined(val pairId: String, val petId: String) : JoinPairUiState
    data class Error(val messageResId: Int) : JoinPairUiState
}

@HiltViewModel
class JoinPairViewModel @Inject constructor(
    private val findPairByInviteKeyUseCase: FindPairByInviteKeyUseCase,
    private val requestJoinUseCase: RequestJoinUseCase,
    private val leaveSessionUseCase: LeaveSessionUseCase,
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
            val guestId = userRepository.getCurrentUserId()
            if (guestId == null) {
                _uiState.value = JoinPairUiState.Error(R.string.error_not_authenticated)
                return@launch
            }

            if (inviteCode.isBlank()) {
                _uiState.value = JoinPairUiState.Error(R.string.error_pair_invalid_input)
                return@launch
            }

            _uiState.value = JoinPairUiState.Searching

            when (val pairResult = findPairByInviteKeyUseCase(inviteCode.uppercase())) {
                is DomainResult.Success -> {
                    val pair = pairResult.data
                    currentPairId = pair.id
                    startObservingPair(pair.id, pair.name)

                    _uiState.value = JoinPairUiState.SendingRequest

                    when (val joinResult = requestJoinUseCase(pair.id, guestId)) {
                        is DomainResult.Success -> {
                            _uiState.updateState { current ->
                                if (current is JoinPairUiState.SendingRequest) {
                                    JoinPairUiState.WaitingForApproval(pairName = pair.name, hostId = pair.userId1)
                                } else current
                            }
                        }
                        is DomainResult.Failure -> {
                            pairObservationJob?.cancel()
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

    private fun startObservingPair(pairId: String, fallbackName: String) {
        pairObservationJob?.cancel()
        pairObservationJob = viewModelScope.launch {
            pairRepository.observePair(pairId)
                .catch { e ->
                    Timber.tag(TAG).e(e, "Observation error for %s", pairId)
                    _uiState.value = JoinPairUiState.Error(R.string.error_unknown)
                }
                .collect { pair ->
                    pair?.let { handlePairUpdate(it) }
                }
        }
    }

    private fun handlePairUpdate(
        petPair: com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetPair
    ) {
        val currentUserId = userRepository.getCurrentUserId() ?: return

        // 1. Host одобрил — userId2 = наш uid
        if (petPair.userId2 == currentUserId) {
            viewModelScope.launch {
                val petId = petPair.currentPetId
                sessionRepository.saveActivePairId(petPair.id)
                sessionRepository.saveActivePetId(petId)
                sessionRepository.savePairStatus(petPair.status.name)
                userRepository.updateUserSession(currentUserId, petId, petPair.id)

                _uiState.value = JoinPairUiState.Joined(pairId = petPair.id, petId = petId)
            }
            return
        }

        // 2. Host отклонил: мы ждали, а pendingRequest исчез и userId2 всё ещё null
        val current = _uiState.value
        if (current is JoinPairUiState.WaitingForApproval &&
            petPair.pendingRequest == null &&
            petPair.userId2 == null &&
            petPair.status.name == PairStatus.PENDING.name
        ) {
            _uiState.value = JoinPairUiState.Error(R.string.error_pair_request_rejected)
            pairObservationJob?.cancel()
            return
        }

        // 3. Пара закончена
        if (petPair.status.name == PairStatus.ENDED.name) {
            _uiState.value = JoinPairUiState.Error(R.string.error_pair_kicked)
            pairObservationJob?.cancel()
        }
    }

    fun leaveSession() {
        val pairId = currentPairId ?: return
        val guestId = userRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            when (val result = leaveSessionUseCase(pairId, guestId)) {
                is DomainResult.Success -> {
                    sessionRepository.clearAllSessionData()
                    resetState()
                }
                is DomainResult.Failure -> {
                    _uiState.value = JoinPairUiState.Error(result.error.toUiErrorStringRes())
                }
            }
        }
    }

    fun resetState() {
        pairObservationJob?.cancel()
        pairObservationJob = null
        currentPairId = null
        _uiState.value = JoinPairUiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        pairObservationJob?.cancel()
    }

    private companion object {
        const val TAG = "JoinPairVM"
    }
}
