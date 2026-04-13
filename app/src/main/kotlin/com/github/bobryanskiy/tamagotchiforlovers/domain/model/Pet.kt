package com.github.bobryanskiy.tamagotchiforlovers.domain.model

import kotlin.time.Instant

data class Pet(
    val id: String,
    val profile: PetProfile,
    val stats: PetStats,
    // val progression: PetProgression
)

data class PetProfile(
    val name: String,
    val ownerUserId: String,
    val createdAt: Long
)

data class PetStats(
    val hunger: Int,
    val energy: Int,
    val cleanliness: Int,
    val happiness: Int,
    val updateAt: Long,
)

//data class PetProgression(
//    val level: Int,
//    val xp: Int,
//    val evolutionStage: String,
//    val updatedAt: Long
//)