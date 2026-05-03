package com.github.bobryanskiy.tamagotchiforlovers.ui.state

/**
 * UI состояние для экрана подключения (ConnectionScreen)
 * Генерация кода, подключение по коду, выбор режима
 */
data class ConnectionUiState(
    val mode: ConnectionMode = ConnectionMode.OFFLINE,
    val inviteCode: String? = null,
    val isGeneratingCode: Boolean = false,
    val isJoining: Boolean = false,
    val joinCodeInput: String = "",
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val successMessage: String? = null
) {
    enum class ConnectionMode {
        OFFLINE,      // Игра без подключения
        ONLINE_HOST,  // Создание игры с подключением
        ONLINE_GUEST  // Подключение к игре
    }
    
    companion object {
        fun loading() = ConnectionUiState(isLoading = true)
        
        fun error(message: String) = ConnectionUiState(
            isError = true,
            errorMessage = message,
            isLoading = false
        )
        
        fun idle(mode: ConnectionMode = ConnectionMode.OFFLINE) = 
            ConnectionUiState(mode = mode)
    }
}
