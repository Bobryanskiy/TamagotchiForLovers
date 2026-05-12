package com.github.bobryanskiy.tamagotchiforlovers.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute {
    @Serializable
    data object Boot: AppRoute
    @Serializable
    data object Main : AppRoute
    @Serializable
    data object CreatePet : AppRoute
    @Serializable
    data object Pet : AppRoute
    @Serializable
    data object Auth : AppRoute
    @Serializable
    data object PairConnect : AppRoute
    @Serializable
    data class Puzzle(val petId: String, val actionType: String) : AppRoute
    @Serializable
    data class CreatePair(val petId: String) : AppRoute
    @Serializable
    data class JoinRequests(val pairId: String) : AppRoute
}