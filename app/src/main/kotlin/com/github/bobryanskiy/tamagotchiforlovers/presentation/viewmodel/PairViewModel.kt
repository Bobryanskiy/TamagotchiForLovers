package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pair
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PendingRequest
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CheckPairStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PairUiState {
    object Loading : PairUiState
    object CreateMode : PairUiState
    data class ManageMode(val pairId: String) : PairUiState
    data class Error(val message: String) : PairUiState
}

data class PairManagementData(
    val pair: Pair? = null,
    val requests: List<PendingRequest> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PairViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository,
    private val pairRepository: PairRepository,
    private val checkPairStatusUseCase: CheckPairStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PairUiState>(PairUiState.Loading)
    val uiState: StateFlow<PairUiState> = _uiState.asStateFlow()

    private var _managementData = MutableStateFlow(PairManagementData())
    val managementData: StateFlow<PairManagementData> = _managementData.asStateFlow()

    init {
        checkInitialStatus()
    }

    private fun checkInitialStatus() {
        viewModelScope.launch {
            when (val result = checkPairStatusUseCase()) {
                is com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.PairNavigationResult.ExistingPair -> {
                    _uiState.value = PairUiState.ManageMode(result.pairId)
                    observePairDetails(result.pairId)
                }
                is com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.PairNavigationResult.NoPair -> {
                    _uiState.value = PairUiState.CreateMode
                }
            }
        }
    }

    private fun observePairDetails(pairId: String) {
        viewModelScope.launch {
//            combine(
//                pairRepository.observePair(pairId),
//                pairRepository.getPendingRequestsFlow(pairId)
//            ) { pair, requests ->
//                PairManagementData(
//                    pair = pair,
//                    requests = requests,
//                    isLoading = false
//                )
//            }.collect { _managementData.value = it }
        }
    }

    fun createPair(petId: String, pairName: String) {
        viewModelScope.launch {
            _uiState.value = PairUiState.Loading
            val userId = userRepository.getCurrentUserId() ?: run {
                _uiState.value = PairUiState.Error("Пользователь не авторизован")
                return@launch
            }

            val result = pairRepository.createPair(userId, pairName, petId)

            if (result is DomainResult.Success) {
                val newPairId = result.data
                val keyResult = pairRepository.generateInviteKey(newPairId)

                if (keyResult is DomainResult.Success) {
                    _uiState.value = PairUiState.ManageMode(newPairId)
                    observePairDetails(newPairId)
                } else {
                    _uiState.value = PairUiState.Error("Ошибка генерации ключа")
                }
            } else {
                _uiState.value = PairUiState.Error("Ошибка создания пары: ${result.getOrNull()}")
            }
        }
    }

    fun acceptRequest(pairId: String, guestId: String) {
        viewModelScope.launch {
//            pairRepository.acceptPlayer(pairId, guestId)
        }
    }

    fun rejectRequest(pairId: String, guestId: String) {
        viewModelScope.launch {
//            pairRepository.removePendingRequest(pairId, guestId)
        }
    }

    fun copyInviteCode(code: String) {
        // Логика копирования может быть здесь или в UI
    }
}