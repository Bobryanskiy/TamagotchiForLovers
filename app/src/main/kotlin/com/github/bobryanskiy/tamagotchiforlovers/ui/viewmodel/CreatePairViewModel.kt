package com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CreatePairUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.GenerateInviteKeyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CreatePairUiState {
    data object Idle : CreatePairUiState
    data object Loading : CreatePairUiState
    data class Success(val inviteCode: String) : CreatePairUiState
    data class Error(val message: String) : CreatePairUiState
}

@HiltViewModel
class CreatePairViewModel @Inject constructor(
    private val createPairUseCase: CreatePairUseCase,
    private val generateInviteKeyUseCase: GenerateInviteKeyUseCase,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreatePairUiState>(CreatePairUiState.Idle)
    val uiState: StateFlow<CreatePairUiState> = _uiState.asStateFlow()

    private var currentPairId: String? = null

    fun createPair(petId: String, pairName: String) {
        viewModelScope.launch {
            _uiState.value = CreatePairUiState.Loading

            val userId = userRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.value = CreatePairUiState.Error("Пользователь не авторизован")
                return@launch
            }
            
            when (val result = createPairUseCase(userId, pairName, petId)) {
                is DomainResult.Success -> {
                    currentPairId = result.data
                    // Generate invite key after creating pair
                    generateInviteKey(result.data)
                }
                is DomainResult.Failure -> {
                    _uiState.value = CreatePairUiState.Error("Ошибка создания пары: ${result.error}")
                }
            }
        }
    }

    private fun generateInviteKey(pairId: String) {
        viewModelScope.launch {
            when (val result = generateInviteKeyUseCase(pairId)) {
                is DomainResult.Success -> {
                    _uiState.value = CreatePairUiState.Success(result.data)
                }
                is DomainResult.Failure -> {
                    _uiState.value = CreatePairUiState.Error("Ошибка генерации кода: ${result.error}")
                }
            }
        }
    }
}
