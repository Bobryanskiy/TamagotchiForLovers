package com.github.bobryanskiy.tamagotchiforlovers

data class PetState(
    var hunger: Int = 100,
    var happiness: Int = 50,
    var lastFedTimestamp: Long = System.currentTimeMillis()
)