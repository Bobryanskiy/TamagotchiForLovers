package com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model

import com.github.bobryanskiy.tamagotchiforlovers.PetState

data class PairData(
    var userId1: String = "",
    var userId2: String = "",
    @field:JvmField
    var isOpen: Boolean = false,
    var petState: PetState = PetState(),
)