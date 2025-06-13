package com.github.bobryanskiy.tamagotchiforlovers.data.title.model

import androidx.annotation.IdRes
import com.github.bobryanskiy.tamagotchiforlovers.data.pet.model.PetState

data class UserPetInfo(
    val pairId: String? = null,
    val petState: PetState? = null,
    val action: Boolean = false,
    @IdRes val dest: Int
)
