package com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model

import com.github.bobryanskiy.tamagotchiforlovers.data.pet.model.PetState

data class PairData(
    var userId1: String = "",
    var userId2: String = "",
    @field:JvmField
    var isOpen: Boolean = false,
    var petState: PetState = PetState(),
)