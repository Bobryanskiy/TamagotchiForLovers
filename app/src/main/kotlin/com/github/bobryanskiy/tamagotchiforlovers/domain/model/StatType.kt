package com.github.bobryanskiy.tamagotchiforlovers.domain.model

enum class StatType {
    HUNGER,
    ENERGY,
    CLEANLINESS,
    HAPPINESS;

    fun displayName(): String = when (this) {
        HUNGER -> "Hunger"
        ENERGY -> "Energy"
        CLEANLINESS -> "Cleanliness"
        HAPPINESS -> "Happiness"
    }
}
