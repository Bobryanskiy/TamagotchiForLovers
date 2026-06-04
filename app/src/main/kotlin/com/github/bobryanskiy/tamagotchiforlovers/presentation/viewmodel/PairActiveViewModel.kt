package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.EndSessionUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.KickPartnerUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.LeaveSessionUseCase
import com.github.bobryanskiy.tamagotchiforlovers.presentation.mapper.toUiErrorStringRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PairActiveUiState {
    data object Loading : PairActiveUiState
    data class Content(
        val pairId: String,
        val pairName: String,
        val isCreator: Boolean,
        val partnerId: String?
    ) : PairActiveUiState
    data object Ended : PairActiveUiState
    data class Error(@param:StringRes val messageResId: Int) : PairActiveUiState
}

@HiltViewModel
class PairActiveViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pairRepository: PairRepository,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val endSessionUseCase: EndSessionUseCase,
    private val kickPartnerUseCase: KickPartnerUseCase,
    private val leaveSessionUseCase: LeaveSessionUseCase
) : ViewModel() {

    private val pairId: String = checkNotNull(savedStateHandle["pairId"])

    private val _uiState = MutableStateFlow<PairActiveUiState>(PairActiveUiState.Loading)
    val uiState: StateFlow<PairActiveUiState> = _uiState.asStateFlow()

    private var pairJob: Job? = null

    init {
        observePair()
    }

    private fun observePair() {
        pairJob = viewModelScope.launch {
            pairRepository.observePair(pairId)
                .catch { /* ignore */ }
                .collect { pair ->
                    when {
                        pair == null -> {
                            _uiState.value = PairActiveUiState.Error(R.string.error_pair_not_found)
                        }
                        pair.status != PairStatus.ACTIVE -> {
                            _uiState.value = PairActiveUiState.Ended
                        }
                        else -> {
                            val currentUserId = userRepository.getCurrentUserId()
                            _uiState.value = PairActiveUiState.Content(
                                pairId = pair.id,
                                pairName = pair.name,
                                isCreator = pair.userId1 == currentUserId,
                                partnerId = if (pair.userId1 == currentUserId) pair.userId2 else pair.userId1
                            )
                        }
                    }
                }
        }
    }

    fun renamePair(newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            when (val result = pairRepository.updatePairName(pairId, newName)) {
                is DomainResult.Success -> { /* UI обновится через observe */ }
                is DomainResult.Failure -> {
                    _uiState.value = PairActiveUiState.Error(result.error.toUiErrorStringRes())
                }
            }
        }
    }

    fun kickPartner() {
        val callerId = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            when (val result = kickPartnerUseCase(pairId, callerId)) {
                is DomainResult.Success -> { /* observePair переведёт в Ended */ }
                is DomainResult.Failure -> {
                    _uiState.value = PairActiveUiState.Error(result.error.toUiErrorStringRes())
                }
            }
        }
    }

    fun endSession() {
        val callerId = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            when (val result = endSessionUseCase(pairId, callerId)) {
                is DomainResult.Success -> {
                    sessionRepository.clearActivePairId()
                    sessionRepository.clearPairStatus()
                }
                is DomainResult.Failure -> {
                    _uiState.value = PairActiveUiState.Error(result.error.toUiErrorStringRes())
                }
            }
        }
    }

    fun leaveSession() {
        val userId = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            when (val result = leaveSessionUseCase(pairId, userId)) {
                is DomainResult.Success -> {
                    sessionRepository.clearActivePairId()
                    sessionRepository.clearPairStatus()
                }
                is DomainResult.Failure -> {
                    _uiState.value = PairActiveUiState.Error(result.error.toUiErrorStringRes())
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pairJob?.cancel()
    }
}
