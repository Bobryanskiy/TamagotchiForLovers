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
    /**
     * Применяет действие к статам.
     * Константы баланса теперь приходят извне через GameBalanceConfig.
     */
    fun applyAction(
        action: PetAction,
        currentTime: Long,
        config: GameBalanceConfig
    ): PetStats = when (action) {
        PetAction.Feed -> copy(
            hunger = (hunger + config.feedHungerBoost).coerceAtMost(100),
            energy = (energy - config.feedEnergyCost).coerceAtLeast(0),
            cleanliness = (cleanliness - config.feedCleanlinessCost).coerceAtLeast(0),
            happiness = (happiness + config.feedHappinessBoost).coerceAtMost(100),
            updatedAt = currentTime
        )
        PetAction.Play -> copy(
            happiness = (happiness + config.playHappinessBoost).coerceAtMost(100),
            energy = (energy - config.playEnergyCost).coerceAtLeast(0),
            hunger = (hunger - config.playHungerCost).coerceAtLeast(0),
            cleanliness = (cleanliness - config.playCleanlinessCost).coerceAtLeast(0),
            updatedAt = currentTime
        )
        PetAction.Clean -> copy(
            cleanliness = (cleanliness + config.cleanCleanlinessBoost).coerceAtMost(100),
            energy = (energy - config.cleanEnergyCost).coerceAtLeast(0),
            happiness = (happiness - config.cleanHappinessCost).coerceAtLeast(0),
            updatedAt = currentTime
        )
        PetAction.Rest -> copy(
            energy = (energy + config.restEnergyBoost).coerceAtMost(100),
            hunger = (hunger - config.restHungerCost).coerceAtLeast(0),
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
    //val recoveryEndTime: Long? = null,
    val deathCause: DeathCause? = null
) {
    fun isTerminal(): Boolean = status == PetLifeStatus.DEAD// || status == PetLifeStatus.ESCAPED
}

enum class PetLifeStatus {
    NORMAL,
    DEAD
}

enum class DeathCause {
    HUNGER,
    EXHAUSTION,
    DISEASE,
    ESCAPED,
}

enum class SyncStatus {
    PENDING, SYNCED
}
