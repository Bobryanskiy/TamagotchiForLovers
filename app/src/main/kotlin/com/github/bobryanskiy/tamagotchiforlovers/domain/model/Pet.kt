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
        // Игровые балансные константы
        const val FEED_HUNGER_BOOST = 30
        const val FEED_HAPPINESS_BOOST = 10
        const val PLAY_HAPPINESS_BOOST = 30
        const val PLAY_ENERGY_COST = 15
        const val PLAY_HUNGER_COST = 10
        const val REST_ENERGY_BOOST = 40
        const val REST_HUNGER_COST = 5
        const val CLEAN_HAPPINESS_BOOST = 5
    }

    fun applyAction(action: PetAction, currentTime: Long): PetStats = when (action) {
        PetAction.Feed -> copy(
            hunger = (hunger + FEED_HUNGER_BOOST).coerceIn(0, 100),
            happiness = (happiness + FEED_HAPPINESS_BOOST).coerceIn(0, 100),
            updatedAt = currentTime
        )
        PetAction.Play -> copy(
            happiness = (happiness + PLAY_HAPPINESS_BOOST).coerceIn(0, 100),
            energy = (energy - PLAY_ENERGY_COST).coerceIn(0, 100),
            hunger = (hunger - PLAY_HUNGER_COST).coerceIn(0, 100),
            updatedAt = currentTime
        )
        PetAction.Clean -> copy(
            cleanliness = 100,
            happiness = (happiness + CLEAN_HAPPINESS_BOOST).coerceIn(0, 100),
            updatedAt = currentTime
        )
        PetAction.Rest -> copy(
            energy = (energy + REST_ENERGY_BOOST).coerceIn(0, 100),
            hunger = (hunger - REST_HUNGER_COST).coerceIn(0, 100),
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
    val isActionsBlocked: Boolean = false,
    // Для COLLAPSED
    val recoveryEndTime: Long? = null,
    val decayMultiplier: Float = 1.0f
) {
    fun isTerminal(): Boolean = status == PetLifeStatus.DEAD || status == PetLifeStatus.ESCAPED
}

enum class PetLifeStatus {
    // Жив и здоров
    NORMAL,
    // Болен (ускоренный декей статов)
    SICK,
    // Без сознания (блокировка действий на время)
    COLLAPSED,
    // Погиб (конец игры)
    DEAD,
    // Убежал (конец игры)
    ESCAPED
}

enum class SyncStatus {
    LOCAL_ONLY, SYNCED, PENDING
}