package com.github.bobryanskiy.tamagotchiforlovers.data.login.model

import com.github.bobryanskiy.tamagotchiforlovers.data.pet.model.PetState

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val userId: String,
    val code: String,
    val petState: PetState,
    val displayName: String?
)