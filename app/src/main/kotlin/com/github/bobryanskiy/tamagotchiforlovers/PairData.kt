package com.github.bobryanskiy.tamagotchiforlovers

data class PairData(
    var userId1: String = "",
    var userId2: String = "",
    @field:JvmField
    var isOpen: Boolean = false,
    var petState: PetState = PetState(),
)