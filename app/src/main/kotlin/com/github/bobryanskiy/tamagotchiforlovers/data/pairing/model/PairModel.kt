package com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model

import com.github.bobryanskiy.tamagotchiforlovers.data.pet.model.PetState

data class PairModel(
    val code: String,
    val petState: PetState,
)