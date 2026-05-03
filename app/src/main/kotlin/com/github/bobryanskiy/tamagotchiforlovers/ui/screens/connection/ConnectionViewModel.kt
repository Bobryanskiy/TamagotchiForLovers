package com.github.bobryanskiy.tamagotchiforlovers.ui.screens.connection

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * UI State для экрана подключения
 */
data class ConnectionUiState(
    val connectionCode: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val generatedCode: String? = null,
    val isHost: Boolean = false
)

/**
 * ViewModel для экрана подключения к онлайн-игре.
 * Управляет созданием и подключением к игровым сессиям.
 */
@HiltViewModel
class ConnectionViewModel @Inject constructor(
    // private val createGameSessionUseCase: CreateGameSessionUseCase,
    // private val joinGameSessionUseCase: JoinGameSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectionUiState())
    val uiState: StateFlow<ConnectionUiState> = _uiState.asStateFlow()

    fun updateConnectionCode(code: String) {
        _uiState.value = _uiState.value.copy(
            connectionCode = code,
            error = null
        )
    }

    /**
     * Создает новую игровую сессию и генерирует код
     */
    fun createGameSession() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        // TODO: Вызвать UseCase для создания игровой сессии
        // viewModelScope.launch {
        //     try {
        //         val code = createGameSessionUseCase.execute()
        //         _uiState.value = _uiState.value.copy(
        //             isLoading = false,
        //             isSuccess = true,
        //             isHost = true,
        //             generatedCode = code
        //         )
        //     } catch (e: Exception) {
        //         _uiState.value = _uiState.value.copy(
        //             isLoading = false,
        //             error = e.message
        //         )
        //     }
        // }
    }

    /**
     * Подключается к существующей игровой сессии по коду
     */
    fun joinGameSession(code: String) {
        if (code.length < 6) {
            _uiState.value = _uiState.value.copy(error = "Некорректный код")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        // TODO: Вызвать UseCase для подключения к сессии
        // viewModelScope.launch {
        //     try {
        //         joinGameSessionUseCase.execute(code)
        //         _uiState.value = _uiState.value.copy(
        //             isLoading = false,
        //             isSuccess = true,
        //             isHost = false
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
        _uiState.value = _uiState.value.copy(
            isSuccess = false,
            generatedCode = null
        )
    }
}
