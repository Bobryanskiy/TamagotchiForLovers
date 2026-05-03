package com.github.bobryanskiy.tamagotchiforlovers.ui.state

/**
 * UI-события для навигации и одноразовых действий
 * Используются в ViewModel для передачи событий в View
 */
sealed class UiEvent {
    // Навигация
    object NavigateToGame : UiEvent()
    object NavigateToLobby : UiEvent()
    object NavigateToHome : UiEvent()
    object NavigateToGameOver : UiEvent()
    object NavigateToCreatePet : UiEvent()
    object NavigateToJoinSession : UiEvent()
    
    // События UI
    data class ShowError(val message: String) : UiEvent()
    data class ShowMessage(val message: String) : UiEvent()
    data class InviteCodeGenerated(val code: String) : UiEvent()
}
