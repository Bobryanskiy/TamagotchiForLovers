package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.DeathCause
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.NotificationKey
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetNotification
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.StatType
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.BalanceConfigRepository
import javax.inject.Inject

class PreparePetNotificationUseCase @Inject constructor(
    private val balanceConfigRepository: BalanceConfigRepository
) {
    operator fun invoke(pet: Pet): PetNotification {
        val config = balanceConfigRepository.get()

        if (pet.lifeState.status == PetLifeStatus.DEAD) {
            val deathKey = when (pet.lifeState.deathCause) {
                DeathCause.HUNGER -> NotificationKey.DeadFromHunger
                DeathCause.EXHAUSTION -> NotificationKey.DeadFromExhaustion
                DeathCause.DISEASE -> NotificationKey.DeadFromDisease
                DeathCause.ESCAPED -> NotificationKey.DeadEscaped
                null -> NotificationKey.Dead
            }

            return PetNotification(
                petId = pet.id,
                petName = pet.profile.name,
                key = deathKey,
                isUrgent = true,
                status = PetLifeStatus.DEAD,
                shouldShow = true
            )
        }

        val stats = pet.stats
        val statsByType: Map<StatType, Int> = mapOf(
            StatType.HUNGER to stats.hunger,
            StatType.ENERGY to stats.energy,
            StatType.CLEANLINESS to stats.cleanliness,
            StatType.HAPPINESS to stats.happiness
        )

        val worstStat = statsByType.minByOrNull { it.value }?.key ?: StatType.HAPPINESS
        val minStatValue = statsByType[worstStat] ?: 0
        val isCritical = minStatValue <= config.criticalThreshold

        val key = when (worstStat) {
            StatType.HUNGER -> if (isCritical) NotificationKey.CriticalHunger else NotificationKey.WarningHunger
            StatType.ENERGY -> if (isCritical) NotificationKey.CriticalEnergy else NotificationKey.WarningEnergy
            StatType.CLEANLINESS -> if (isCritical) NotificationKey.CriticalCleanliness else NotificationKey.WarningCleanliness
            StatType.HAPPINESS -> if (isCritical) NotificationKey.CriticalHappiness else NotificationKey.WarningHappiness
        }

        return PetNotification(
            petId = pet.id,
            petName = pet.profile.name,
            key = key,
            isUrgent = isCritical,
            status = pet.lifeState.status,
            shouldShow = true
        )
    }
}
