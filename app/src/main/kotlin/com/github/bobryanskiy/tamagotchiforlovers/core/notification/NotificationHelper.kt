package com.github.bobryanskiy.tamagotchiforlovers.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeStatus
import com.github.bobryanskiy.tamagotchiforlovers.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val logger: AppLogger
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "pet_needs_channel"

    companion object {
        private const val TAG = "NotificationHelper"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "Нужды питомца",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Напоминания о кормлении, болезни и критических состояниях"
            enableVibration(true)
            enableLights(true)
            setShowBadge(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    data class NotificationData(
        val petId: String,
        val petName: String,
        val title: String,
        val message: String,
        val isUrgent: Boolean,
        val status: PetLifeStatus
    )

    fun showPetNotification(data: NotificationData) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            logger.w(TAG, "Notifications are disabled, skipping")
            return
        }

        val iconRes = when (data.status) {
            PetLifeStatus.SICK -> R.drawable.ic_pet_icon
            PetLifeStatus.COLLAPSED -> R.drawable.ic_pet_icon
            PetLifeStatus.DEAD -> R.drawable.ic_pet_icon
            PetLifeStatus.ESCAPED -> R.drawable.ic_pet_icon
            PetLifeStatus.NORMAL -> R.drawable.ic_pet_icon
        }

        val priority = if (data.isUrgent) {
            NotificationCompat.PRIORITY_MAX
        } else {
            NotificationCompat.PRIORITY_DEFAULT
        }

        val category = if (data.isUrgent) {
            NotificationCompat.CATEGORY_ALARM
        } else {
            NotificationCompat.CATEGORY_REMINDER
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("open_pet_id", data.petId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            data.petId.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(data.title)
            .setContentText(data.message)
            .setPriority(priority)
            .setCategory(category)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(data.message))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        try {
            notificationManager.notify("pet_notification_${data.petId}", data.petId.hashCode(), notification)
            logger.d(TAG, "Notification shown for pet: ${data.petId}")
        } catch (e: SecurityException) {
            logger.e(TAG, "Failed to show notification: missing POST_NOTIFICATIONS permission", e)
        } catch (e: Exception) {
            logger.e(TAG, "Failed to show notification", e)
        }
    }

    fun cancelNotification(petId: String) {
        try {
            notificationManager.cancel("pet_notification_$petId", petId.hashCode())
            logger.d(TAG, "Notification cancelled for pet: $petId")
        } catch (e: Exception) {
            logger.e(TAG, "Failed to cancel notification", e)
        }
    }
}