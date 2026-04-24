package com.github.bobryanskiy.tamagotchiforlovers.domain.model

data class Pet(
    val id: String,
    val profile: PetProfile,
    val stats: PetStats,
    // val progression: PetProgression
)

data class PetProfile(
    val name: String,
    val ownerUserId: String,
    val currentPairId: String?,
    val createdAt: Long,
    val criticalStatus: PetCriticalStatus,
    val recoveryEndTime: Long?,
    val abandonedAt: Long?
)

data class PetStats(
    val hunger: Int,
    val energy: Int,
    val cleanliness: Int,
    val happiness: Int,
    val updatedAt: Long,
)

//data class PetProgression(
//    val level: Int,
//    val xp: Int,
//    val evolutionStage: String,
//    val updatedAt: Long
//)