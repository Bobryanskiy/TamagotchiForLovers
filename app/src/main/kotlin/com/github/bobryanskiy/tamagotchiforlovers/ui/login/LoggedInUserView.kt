package com.github.bobryanskiy.tamagotchiforlovers.ui.login

import com.github.bobryanskiy.tamagotchiforlovers.data.pet.model.PetState

/**
 * User details post authentication that is exposed to the UI
 */
data class LoggedInUserView(
    val pairCode: String,
    val petState: PetState,
    val displayName: String?
    //... other data fields that may be accessible to the UI
)