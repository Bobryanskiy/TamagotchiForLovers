package com.github.bobryanskiy.tamagotchiforlovers.domain.model

object PetCriticalConfig {
    fun get(status: PetLifeStatus) = when (status) {
        PetLifeStatus.NORMAL -> CriticalStateData(1.0f, isActionsBlocked = false)
        PetLifeStatus.SICK   -> CriticalStateData(2.0f, isActionsBlocked = false)
        PetLifeStatus.COLLAPSED -> CriticalStateData(3.0f, isActionsBlocked = true)
        PetLifeStatus.ESCAPED -> CriticalStateData(1.0f, isActionsBlocked = true)
        PetLifeStatus.DEAD    -> CriticalStateData(0.0f, isActionsBlocked = true)
    }

    data class CriticalStateData(
        val decayMultiplier: Float,
        val isActionsBlocked: Boolean
    )
}