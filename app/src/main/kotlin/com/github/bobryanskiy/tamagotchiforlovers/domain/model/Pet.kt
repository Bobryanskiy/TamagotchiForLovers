package com.github.bobryanskiy.tamagotchiforlovers.domain.model

data class Pet(
    val petId: String = "",
    val pairId: String = "",
    val hunger: Int = 100,
    val energy: Int = 100,
    val cleanliness: Int = 100,
    val happiness: Int = 100
)
