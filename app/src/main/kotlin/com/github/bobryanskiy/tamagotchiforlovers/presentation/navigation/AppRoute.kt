package com.github.bobryanskiy.tamagotchiforlovers.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoute {
    @Serializable
    object Boot: AppRoute
    @Serializable
    object Main : AppRoute
    @Serializable
    object CreatePet : AppRoute
    @Serializable
    data class Pet(val petId: String): AppRoute
    @Serializable
    data class Profile(val petId: String) : AppRoute
    @Serializable
    object Auth : AppRoute
    @Serializable
    object PairConnect : AppRoute
    @Serializable
    data class CreatePair(val petId: String) : AppRoute
    @Serializable
    data class JoinRequests(val pairId: String) : AppRoute
}