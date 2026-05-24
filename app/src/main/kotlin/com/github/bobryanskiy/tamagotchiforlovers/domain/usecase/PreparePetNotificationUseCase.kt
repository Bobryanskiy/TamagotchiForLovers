package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.NotificationKey
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetNotification
import javax.inject.Inject

/**
 * Готовит данные для уведомления.
 *
 * ❗ ЧИСТЫЙ Domain: никакого R.string, никакого Context.
 * Возвращает NotificationKey — ключ, который Android-слой превратит в строку.
 */
class PreparePetNotificationUseCase @Inject constructor() {

    operator fun invoke(pet: Pet): PetNotification {
        val stats = pet.stats
        val minStat = minOf(stats.hunger, stats.energy, stats.cleanliness, stats.happiness)
        val isCritical = minStat <= 15

        // Terminal states
        if (pet.lifeState.status == PetLifeStatus.DEAD) {
            return PetNotification(
                petId = pet.id,
                petName = pet.profile.name,
                titleKey = NotificationKey.Dead,
                messageKey = NotificationKey.Dead,  // одно и то же сообщение
                isUrgent = true,
                status = PetLifeStatus.DEAD
            )
        }
        if (pet.lifeState.status == PetLifeStatus.ESCAPED) {
            return PetNotification(
                petId = pet.id,
                petName = pet.profile.name,
                titleKey = NotificationKey.Escaped,
                messageKey = NotificationKey.Escaped,
                isUrgent = true,
                status = PetLifeStatus.ESCAPED
            )
        }

        // Live pet — определяем критичность по минимальной статистике
        val (titleKey, messageKey) = when (minStat) {
            stats.hunger -> if (isCritical)
                NotificationKey.CriticalHunger to NotificationKey.CriticalHunger
            else NotificationKey.WarningHunger to NotificationKey.WarningHunger
            stats.energy -> if (isCritical)
                NotificationKey.CriticalEnergy to NotificationKey.CriticalEnergy
            else NotificationKey.WarningEnergy to NotificationKey.WarningEnergy
            stats.cleanliness -> if (isCritical)
                NotificationKey.CriticalCleanliness to NotificationKey.CriticalCleanliness
            else NotificationKey.WarningCleanliness to NotificationKey.WarningCleanliness
            else -> if (isCritical)
                NotificationKey.CriticalHappiness to NotificationKey.CriticalHappiness
            else NotificationKey.WarningHappiness to NotificationKey.WarningHappiness
        }

        return PetNotification(
            petId = pet.id,
            petName = pet.profile.name,
            titleKey = titleKey,
            messageKey = messageKey,
            isUrgent = isCritical,
            status = pet.lifeState.status
        )
    }
}