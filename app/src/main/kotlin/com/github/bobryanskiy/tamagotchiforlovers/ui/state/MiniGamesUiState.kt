package com.github.bobryanskiy.tamagotchiforlovers.ui.state

/**
 * UI состояние для экрана мини-игр (MiniGamesScreen)
 * Для увеличения характеристик через игры
 */
data class MiniGamesUiState(
    val selectedStat: StatType? = null,
    val currentGame: GameType? = null,
    val gameScore: Int = 0,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val rewardEarned: Int = 0
) {
    enum class StatType {
        HUNGER,       // Кормление
        HAPPINESS,    // Развлечение
        ENERGY,       // Сон/отдых
        HEALTH        // Лечение
    }
    
    enum class GameType {
        MATH_FEED,    // Математика для кормления
        MEMORY_GAME,  // Игра на память
        REFLEX_GAME,  // Игра на реакцию
        QUIZ_GAME     // Викторина
    }
    
    companion object {
        fun loading() = MiniGamesUiState(isLoading = true)
        
        fun error(message: String) = MiniGamesUiState(
            isError = true,
            errorMessage = message,
            isLoading = false
        )
        
        fun idle() = MiniGamesUiState()
    }
}
