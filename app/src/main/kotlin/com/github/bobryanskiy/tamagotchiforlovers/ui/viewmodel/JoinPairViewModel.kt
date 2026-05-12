package com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.RequestJoinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface JoinPairUiState {
    data object Idle : JoinPairUiState
    data object Loading : JoinPairUiState
    data class Success(val petId: String) : JoinPairUiState
    data class Error(val message: String) : JoinPairUiState
}

@HiltViewModel
class JoinPairViewModel @Inject constructor(
    private val requestJoinUseCase: RequestJoinUseCase,
    private val userRepository: UserRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<JoinPairUiState>(JoinPairUiState.Idle)
    val uiState: StateFlow<JoinPairUiState> = _uiState.asStateFlow()

    fun joinPair(inviteCode: String) {
        viewModelScope.launch {
            _uiState.value = JoinPairUiState.Loading

            when (val result = requestJoinUseCase(inviteCode)) {
                is DomainResult.Success -> {
                    val userId = userRepository.getCurrentUserId()
                    if (userId != null) {
                        val user = userRepository.observeUser(userId).first()
                        val petId = user?.activePetId

                        if (petId != null) {
                            _uiState.value = JoinPairUiState.Success(petId)
                        } else {
                            _uiState.value = JoinPairUiState.Error("Питомец не найден. Создайте питомца сначала.")
                        }
                    } else {
                        _uiState.value = JoinPairUiState.Error("Пользователь не авторизован")
                    }
                }
                is DomainResult.Failure -> {
                    _uiState.value = JoinPairUiState.Error("Ошибка присоединения: ${result.error}")
                }
            }
        }
    }
}