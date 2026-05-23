package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.core.notification.NotificationHelper
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreparePetNotificationUseCase @Inject constructor(
    private val notificationHelper: NotificationHelper
) {
    suspend operator fun invoke(pet: Pet, stringProvider: StringResourceProvider) {
        try {
            val data = prepareNotificationData(pet, stringProvider)
            notificationHelper.showPetNotification(data)
        } catch (e: Exception) {
            // Логирование должно быть через интерфейс или удалено из domain
        }
    }

    private fun prepareNotificationData(pet: Pet, stringProvider: StringResourceProvider): NotificationHelper.NotificationData {
        if (pet.lifeState.status == PetLifeStatus.DEAD) {
            return NotificationHelper.NotificationData(
                petId = pet.id,
                petName = pet.profile.name,
                title = stringProvider.getString(R.string.notif_dead, pet.profile.name),
                message = stringProvider.getString(R.string.notif_dead_message),
                isUrgent = true,
                status = PetLifeStatus.DEAD
            )
        }

        if (pet.lifeState.status == PetLifeStatus.ESCAPED) {
            return NotificationHelper.NotificationData(
                petId = pet.id,
                petName = pet.profile.name,
                title = stringProvider.getString(R.string.notif_escaped, pet.profile.name),
                message = stringProvider.getString(R.string.notif_escaped_message),
                isUrgent = true,
                status = PetLifeStatus.ESCAPED
            )
        }

        val stats = pet.stats
        val minStat = minOf(stats.hunger, stats.energy, stats.cleanliness, stats.happiness)

        val titleResId = if (minStat <= 15) {
            R.string.notif_title_crit
        } else {
            R.string.notif_title_warn
        }

        val textResId = when (minStat) {
            stats.hunger -> if (minStat <= 15) R.string.notif_crit_hunger else R.string.notif_warn_hunger
            stats.energy -> if (minStat <= 15) R.string.notif_crit_energy else R.string.notif_warn_energy
            stats.cleanliness -> if (minStat <= 15) R.string.notif_crit_clean else R.string.notif_warn_clean
            stats.happiness -> if (minStat <= 15) R.string.notif_crit_happy else R.string.notif_warn_happy
            else -> R.string.notif_warn_happy
        }

        val title = stringProvider.getString(titleResId, pet.profile.name)
        val message = stringProvider.getString(textResId, pet.profile.name)
        val isUrgent = minStat <= 15

        return NotificationHelper.NotificationData(
            petId = pet.id,
            petName = pet.profile.name,
            title = title,
            message = message,
            isUrgent = isUrgent,
            status = pet.lifeState.status
        )
    }
}

interface StringResourceProvider {
    fun getString(resId: Int, vararg formatArgs: String): String
}