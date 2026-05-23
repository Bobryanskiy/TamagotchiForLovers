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
    fun applyAction(action: PetAction, currentTime: Long): PetStats {
        return when (action) {
            PetAction.Feed -> copy(
                hunger = (hunger + 30).coerceIn(0, 100),
                happiness = (happiness + 10).coerceIn(0, 100),
                updatedAt = currentTime
            )
            PetAction.Play -> copy(
                happiness = (happiness + 30).coerceIn(0, 100),
                energy = (energy - 15).coerceIn(0, 100),
                hunger = (hunger + 10).coerceIn(0, 100),
                updatedAt = currentTime
            )
            PetAction.Clean -> copy(
                cleanliness = 100,
                happiness = (happiness + 5).coerceIn(0, 100),
                updatedAt = currentTime
            )
            PetAction.Rest -> copy(
                energy = (energy + 40).coerceIn(0, 100),
                hunger = (hunger + 5).coerceIn(0, 100),
                updatedAt = currentTime
            )
        }
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
    SYNCED, PENDING
}