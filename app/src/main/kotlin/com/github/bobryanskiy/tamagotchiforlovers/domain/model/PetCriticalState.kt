package com.github.bobryanskiy.tamagotchiforlovers.domain.model

data class PetCriticalState(
    val status: PetCriticalStatus = PetCriticalStatus.NORMAL,
    val decayMultiplier: Float = 1.0f,
    val recoveryEndTime: Long? = null,
    val isActionsBlocked: Boolean = false
)