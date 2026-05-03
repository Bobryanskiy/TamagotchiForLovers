package com.example.petgame.ui.screens.login

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * UI State для экрана входа
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel для экрана входа.
 * Управляет аутентификацией пользователя.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    // private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            error = null
        )
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            error = null
        )
    }

    fun login() {
        val currentState = _uiState.value
        
        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _uiState.value = currentState.copy(error = "Заполните все поля")
            return
        }

        _uiState.value = currentState.copy(isLoading = true)

        // TODO: Вызвать UseCase для аутентификации
        // viewModelScope.launch {
        //     try {
        //         loginUseCase.execute(currentState.email, currentState.password)
        //         _uiState.value = _uiState.value.copy(
        //             isLoading = false,
        //             isSuccess = true
        //         )
        //     } catch (e: Exception) {
        //         _uiState.value = _uiState.value.copy(
        //             isLoading = false,
        //             error = e.message
        //         )
        //     }
        // }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }
}
