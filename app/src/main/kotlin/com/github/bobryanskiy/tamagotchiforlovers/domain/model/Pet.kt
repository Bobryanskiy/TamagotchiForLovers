package com.github.bobryanskiy.tamagotchiforlovers.domain.model

data class Pet(
    val id: String,
    val profile: PetProfile,
    val stats: PetStats,
    val lifeState: PetLifeState,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
    // val progression: PetProgression
)

data class PetProfile(
    val name: String,
    val ownerUserId: String?,
    val currentPairId: String?,
    val createdAt: Long,
    val abandonedAt: Long?,
)

data class PetStats(
    val hunger: Int,
    val energy: Int,
    val cleanliness: Int,
    val happiness: Int,
    val updatedAt: Long
) {
    companion object {
        const val FEED_HUNGER_BOOST = 30
        const val FEED_HAPPINESS_BOOST = 10
        const val FEED_ENERGY_COST = 5
        const val FEED_CLEANLINESS_COST = 3

        const val PLAY_HAPPINESS_BOOST = 25
        const val PLAY_ENERGY_COST = 15
        const val PLAY_HUNGER_COST = 10
        const val PLAY_CLEANLINESS_COST = 5

        const val CLEAN_CLEANLINESS_BOOST = 40
        const val CLEAN_ENERGY_COST = 8
        const val CLEAN_HAPPINESS_COST = 5

        const val REST_ENERGY_BOOST = 35
        const val REST_HUNGER_COST = 8
    }

    fun applyAction(action: PetAction, currentTime: Long): PetStats = when (action) {
        PetAction.Feed -> copy(
            hunger = (hunger + FEED_HUNGER_BOOST).coerceAtMost(100),
            energy = (energy - FEED_ENERGY_COST).coerceAtLeast(0),
            cleanliness = (cleanliness - FEED_CLEANLINESS_COST).coerceAtLeast(0),
            updatedAt = currentTime
        )
        PetAction.Play -> copy(
            happiness = (happiness + PLAY_HAPPINESS_BOOST).coerceAtMost(100),
            energy = (energy - PLAY_ENERGY_COST).coerceAtLeast(0),
            hunger = (hunger - PLAY_HUNGER_COST).coerceAtLeast(0),
            cleanliness = (cleanliness - PLAY_CLEANLINESS_COST).coerceAtLeast(0),
            updatedAt = currentTime
        )
        PetAction.Clean -> copy(
            cleanliness = (cleanliness + CLEAN_CLEANLINESS_BOOST).coerceAtMost(100),
            energy = (energy - CLEAN_ENERGY_COST).coerceAtLeast(0),
            happiness = (happiness - CLEAN_HAPPINESS_COST).coerceAtLeast(0),
            updatedAt = currentTime
        )
        PetAction.Rest -> copy(
            energy = (energy + REST_ENERGY_BOOST).coerceAtMost(100),
            hunger = (hunger - REST_HUNGER_COST).coerceAtLeast(0),
            updatedAt = currentTime
        )
    }
}

//data class PetProgression(
//    val level: Int,
//    val xp: Int,
//    val evolutionStage: String,
//    val updatedAt: Long
//)

data class PetLifeState(
    val status: PetLifeStatus,
    val recoveryEndTime: Long? = null,
    val decayMultiplier: Float = 1.0f
) {
    fun isTerminal(): Boolean = status == PetLifeStatus.DEAD || status == PetLifeStatus.ESCAPED
}

enum class PetLifeStatus {
    NORMAL,
    SICK,
    COLLAPSED,
    DEAD,
    ESCAPED
}

enum class SyncStatus {
    LOCAL_ONLY, SYNCED, PENDING
}