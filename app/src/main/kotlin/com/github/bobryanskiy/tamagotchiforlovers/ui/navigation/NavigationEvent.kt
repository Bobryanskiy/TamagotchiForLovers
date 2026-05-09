package com.github.bobryanskiy.tamagotchiforlovers.ui.navigation

sealed class NavigationEvent {
    data object NavigateToPet : NavigationEvent() {

    }
}