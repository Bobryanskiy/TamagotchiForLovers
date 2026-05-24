package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.NotificationKey
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetNotification
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.StatType
import javax.inject.Inject

/**
 * Готовит данные для уведомления.
 *
 * ❗ ЧИСТЫЙ Domain: никакого R.string, никакого Context.
 * Возвращает NotificationKey — ключ, который Android-слой превратит в строку.
 */
class PreparePetNotificationUseCase @Inject constructor() {
    companion object {
        /** Порог критичности: стат ниже этого значения → критическое уведомление */
        private const val CRITICAL_THRESHOLD = 15
    }

    operator fun invoke(pet: Pet): PetNotification {
        val stats = pet.stats

        when (pet.lifeState.status) {
            PetLifeStatus.DEAD -> return PetNotification(
                petId = pet.id,
                petName = pet.profile.name,
                key = NotificationKey.Dead,
                isUrgent = true,
                status = PetLifeStatus.DEAD,
                shouldShow = true
            )
            PetLifeStatus.ESCAPED -> return PetNotification(
                petId = pet.id,
                petName = pet.profile.name,
                key = NotificationKey.Escaped,
                isUrgent = true,
                status = PetLifeStatus.ESCAPED,
                shouldShow = true
            )
            PetLifeStatus.SICK -> return PetNotification(
                petId = pet.id,
                petName = pet.profile.name,
                key = NotificationKey.Sick,
                isUrgent = true,
                status = PetLifeStatus.SICK,
                shouldShow = true
            )
            PetLifeStatus.COLLAPSED -> return PetNotification(
                petId = pet.id,
                petName = pet.profile.name,
                key = NotificationKey.Collapsed,
                isUrgent = true,
                status = PetLifeStatus.COLLAPSED,
                shouldShow = true
            )
            PetLifeStatus.NORMAL -> { /* продолжаем ниже */ }
        }

        // Live pet — определяем критичность по минимальной статистике
        val statsByType: Map<StatType, Int> = mapOf(
            StatType.HUNGER to stats.hunger,
            StatType.ENERGY to stats.energy,
            StatType.CLEANLINESS to stats.cleanliness,
            StatType.HAPPINESS to stats.happiness
        )

        val worstStat = statsByType.minByOrNull { it.value }?.key ?: StatType.HAPPINESS
        val minStatValue = statsByType[worstStat] ?: 0
        val isCritical = minStatValue <= CRITICAL_THRESHOLD

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