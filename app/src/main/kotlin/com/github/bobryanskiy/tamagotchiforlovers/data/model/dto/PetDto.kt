package com.github.bobryanskiy.tamagotchiforlovers.data.model.dto

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetCriticalStatus

data class PetDto(
    val profile: ProfileDto? = null,
    val stats: StatsDto? = null,
)

data class ProfileDto(
    val name: String = "",
    val ownerUserId: String = "",
    val currentPairId: String? = null,
    val createdAt: Long = 0L,
    val criticalStatus: PetCriticalStatus = PetCriticalStatus.NORMAL,
    val recoveryEndTime: Long? = null,
    val abandonedAt: Long? = null
)

data class StatsDto(
    val hunger: Int = 50,
    val energy: Int = 50,
    val cleanliness: Int = 50,
    val happiness: Int = 50,
    val updatedAt: Long? = null
)