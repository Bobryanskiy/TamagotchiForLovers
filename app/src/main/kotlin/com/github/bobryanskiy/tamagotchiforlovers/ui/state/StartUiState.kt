package com.github.bobryanskiy.tamagotchiforlovers.ui.state

/**
 * UI состояние для главного экрана (StartScreen)
 */
data class StartUiState(
    val hasActivePet: Boolean = false,
    val hasActivePair: Boolean = false,
    val isGameActive: Boolean = false,
    val isLoading: Boolean = true,
    val userName: String? = null
) {
    companion object {
        fun loading() = StartUiState(isLoading = true)
        
        fun fromData(
            hasPet: Boolean,
            hasPair: Boolean,
            pairStatus: String? = null,
            userName: String? = null
        ) = StartUiState(
            hasActivePet = hasPet,
            hasActivePair = hasPair,
            isGameActive = pairStatus == "ACTIVE",
            isLoading = false,
            userName = userName
        )
    }
}
