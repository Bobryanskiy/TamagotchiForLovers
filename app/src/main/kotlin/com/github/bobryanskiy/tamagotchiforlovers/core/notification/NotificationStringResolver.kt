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
        return when (notification.titleKey) {
            NotificationKey.Dead -> context.getString(R.string.notif_dead, petName)
            NotificationKey.Escaped -> context.getString(R.string.notif_escaped, petName)
            NotificationKey.CriticalHunger -> context.getString(R.string.notif_title_crit, petName)
            NotificationKey.CriticalEnergy -> context.getString(R.string.notif_title_crit, petName)
            NotificationKey.CriticalCleanliness -> context.getString(R.string.notif_title_crit, petName)
            NotificationKey.CriticalHappiness -> context.getString(R.string.notif_title_crit, petName)
            NotificationKey.WarningHunger -> context.getString(R.string.notif_title_warn, petName)
            NotificationKey.WarningEnergy -> context.getString(R.string.notif_title_warn, petName)
            NotificationKey.WarningCleanliness -> context.getString(R.string.notif_title_warn, petName)
            NotificationKey.WarningHappiness -> context.getString(R.string.notif_title_warn, petName)
        }
    }

    fun resolveMessage(notification: PetNotification): String {
        val petName = notification.petName
        return when (notification.messageKey) {
            NotificationKey.Dead -> context.getString(R.string.notif_dead_message)
            NotificationKey.Escaped -> context.getString(R.string.notif_escaped_message)
            NotificationKey.CriticalHunger -> context.getString(R.string.notif_crit_hunger, petName)
            NotificationKey.CriticalEnergy -> context.getString(R.string.notif_crit_energy, petName)
            NotificationKey.CriticalCleanliness -> context.getString(R.string.notif_crit_clean, petName)
            NotificationKey.CriticalHappiness -> context.getString(R.string.notif_crit_happy, petName)
            NotificationKey.WarningHunger -> context.getString(R.string.notif_warn_hunger, petName)
            NotificationKey.WarningEnergy -> context.getString(R.string.notif_warn_energy, petName)
            NotificationKey.WarningCleanliness -> context.getString(R.string.notif_warn_clean, petName)
            NotificationKey.WarningHappiness -> context.getString(R.string.notif_warn_happy, petName)
        }
    }
}