package com.github.bobryanskiy.tamagotchiforlovers.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute {
    @Serializable
    data object Boot: AppRoute
    @Serializable
    data object Main : AppRoute
    @Serializable
    data object Pet : AppRoute
    @Serializable
    data object Auth : AppRoute
}