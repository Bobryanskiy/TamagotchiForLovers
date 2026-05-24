package com.github.bobryanskiy.tamagotchiforlovers.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {
    @Serializable data object Boot : AppRoute
    @Serializable data object Auth : AppRoute
    @Serializable data object Main : AppRoute
    @Serializable data object CreatePet : AppRoute

    @Serializable data class Pet(val petId: String) : AppRoute
    @Serializable data object Profile : AppRoute

    // Три состояния пары
    @Serializable data class CreatePair(val petId: String) : AppRoute
    @Serializable data class PairWaiting(val pairId: String) : AppRoute
    @Serializable data class PairActive(val pairId: String) : AppRoute

    @Serializable data object JoinPair : AppRoute

    @Serializable data class RenamePet(val petId: String) : AppRoute
}