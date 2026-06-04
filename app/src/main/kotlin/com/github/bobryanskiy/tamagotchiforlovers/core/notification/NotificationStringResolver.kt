package com.github.bobryanskiy.tamagotchiforlovers.core.notification

import android.content.Context
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.NotificationKey
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetNotification
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Резолвер NotificationKey → String.
 * Живёт в Android-слое, знает о R.string.
 */
class NotificationStringResolver @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun resolveTitle(notification: PetNotification): String {
        val petName = notification.petName
        return when (notification.key) {
            NotificationKey.WarningHunger,
            NotificationKey.WarningEnergy,
            NotificationKey.WarningCleanliness,
            NotificationKey.WarningHappiness ->
                context.getString(R.string.notif_title_warn, petName)

            NotificationKey.CriticalHunger,
            NotificationKey.CriticalEnergy,
            NotificationKey.CriticalCleanliness,
            NotificationKey.CriticalHappiness ->
                context.getString(R.string.notif_title_crit, petName)

            NotificationKey.Dead ->
                context.getString(R.string.notif_dead, petName)
            NotificationKey.DeadFromHunger ->
                context.getString(R.string.notif_dead_from_hunger, petName)
            NotificationKey.DeadFromExhaustion ->
                context.getString(R.string.notif_dead_from_exhaustion, petName)
            NotificationKey.DeadFromDisease ->
                context.getString(R.string.notif_dead_from_disease, petName)
            NotificationKey.DeadEscaped ->
                context.getString(R.string.notif_dead_from_depression, petName)
        }
    }

    fun resolveMessage(notification: PetNotification): String {
        val petName = notification.petName
        return when (notification.key) {
            NotificationKey.WarningHunger ->
                context.getString(R.string.notif_warn_hunger, petName)
            NotificationKey.WarningEnergy ->
                context.getString(R.string.notif_warn_energy, petName)
            NotificationKey.WarningCleanliness ->
                context.getString(R.string.notif_warn_clean, petName)
            NotificationKey.WarningHappiness ->
                context.getString(R.string.notif_warn_happy, petName)

            NotificationKey.CriticalHunger ->
                context.getString(R.string.notif_crit_hunger, petName)
            NotificationKey.CriticalEnergy ->
                context.getString(R.string.notif_crit_energy, petName)
            NotificationKey.CriticalCleanliness ->
                context.getString(R.string.notif_crit_clean, petName)
            NotificationKey.CriticalHappiness ->
                context.getString(R.string.notif_crit_happy, petName)

            NotificationKey.Dead ->
                context.getString(R.string.notif_dead_message, petName)
            NotificationKey.DeadFromHunger ->
                context.getString(R.string.notif_dead_from_hunger_message, petName)
            NotificationKey.DeadFromExhaustion ->
                context.getString(R.string.notif_dead_from_exhaustion_message, petName)
            NotificationKey.DeadFromDisease ->
                context.getString(R.string.notif_dead_from_disease_message, petName)
            NotificationKey.DeadEscaped ->
                context.getString(R.string.notif_dead_from_depression_message, petName)
        }
    }
}
