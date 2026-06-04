package com.github.bobryanskiy.tamagotchiforlovers.presentation.navigation

sealed class NavigationEvent {
    data object NavigateToPet : NavigationEvent()
}
