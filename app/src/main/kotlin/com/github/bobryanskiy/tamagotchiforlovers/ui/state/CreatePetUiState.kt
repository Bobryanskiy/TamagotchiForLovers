package com.github.bobryanskiy.tamagotchiforlovers.ui.state

/**
 * UI состояние для экрана создания питомца (CreatePetScreen)
 */
data class CreatePetUiState(
    val petName: String = "",
    val pairName: String = "",
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val isForPair: Boolean = false,
    val isSuccess: Boolean = false
) {
    companion object {
        fun loading(isForPair: Boolean = false) = CreatePetUiState(
            isLoading = true,
            isForPair = isForPair
        )
        
        fun idle(isForPair: Boolean = false) = CreatePetUiState(
            isForPair = isForPair
        )
        
        fun error(message: String, isForPair: Boolean = false) = CreatePetUiState(
            isError = true,
            errorMessage = message,
            isForPair = isForPair
        )
    }
}
