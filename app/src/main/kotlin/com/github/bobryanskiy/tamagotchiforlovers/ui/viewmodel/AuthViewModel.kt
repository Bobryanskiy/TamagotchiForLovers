package com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.LoginUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val uid: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            loginUseCase(email, password)
                .onSuccess { uid ->
                    _uiState.value = AuthUiState.Success(uid)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            registerUseCase(email, password)
                .onSuccess { uid ->
                    _uiState.value = AuthUiState.Success(uid)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Unknown error")
                }
        }
    }
}