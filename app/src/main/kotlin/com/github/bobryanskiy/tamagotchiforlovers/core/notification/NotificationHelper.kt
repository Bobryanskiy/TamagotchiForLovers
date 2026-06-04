package com.github.bobryanskiy.tamagotchiforlovers.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SettingsRepository
import com.github.bobryanskiy.tamagotchiforlovers.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val logger: AppLogger
) {

    companion object {
        private const val TAG = "NotificationHelper"

        // Два канала: со звуком (для критичных) и тихий
        private const val CHANNEL_ID_URGENT = "pet_urgent"
        private const val CHANNEL_ID_NORMAL = "pet_normal"
        private const val CHANNEL_ID_SILENT = "pet_silent"
    }

    init {
        createNotificationChannels()
    }

    /**
     * Показывает уведомление о состоянии питомца.
     *
     * Проверяет настройки перед показом:
     * - Если notificationsEnabled=false → не показываем
     * - Если soundEnabled=false → используем тихий канал
     */
    suspend fun showPetNotification(data: NotificationData) {
        val notificationsEnabled = settingsRepository.observeNotificationsEnabled().first()

        if (!notificationsEnabled) {
            logger.d(TAG, "🔕 Notifications disabled, skipping: ${data.petName}")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = context.checkSelfPermission(
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permission != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                logger.w(TAG, "⚠️ POST_NOTIFICATIONS permission not granted")
                return
            }
        }

        val soundEnabled = settingsRepository.observeSoundEnabled().first()

        val channelId = when {
            !data.isUrgent -> CHANNEL_ID_SILENT      // обычные — всегда тихо
            soundEnabled -> CHANNEL_ID_URGENT         // критичные + звук включён
            else -> CHANNEL_ID_NORMAL                 // критичные, но без звука
        }

        logger.d(TAG, "🔔 Showing notification for ${data.petName}, channel=$channelId")

        val notification = buildNotification(data, channelId)

        try {
            NotificationManagerCompat.from(context)
                .notify(data.petId.hashCode(), notification)
        } catch (e: SecurityException) {
            logger.e(TAG, "Failed to show notification: permission denied", e)
        }
    }

    private fun buildNotification(data: NotificationData, channelId: String): android.app.Notification {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("petId", data.petId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            data.petId.hashCode(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_hse_bird_idle)
            .setContentTitle(data.title)
            .setContentText(data.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(data.message))
            .setPriority(
                if (data.isUrgent) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }

    private fun createNotificationChannels() {
        val manager = context.getSystemService(NotificationManager::class.java)

        // Канал для критичных уведомлений СО ЗВУКОМ
        val urgentChannel = NotificationChannel(
            CHANNEL_ID_URGENT,
            context.getString(R.string.notif_channel_urgent),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notif_channel_urgent_desc)
            enableVibration(true)
            // Звук по умолчанию
        }

        // Канал для критичных уведомлений БЕЗ ЗВУКА
        val normalChannel = NotificationChannel(
            CHANNEL_ID_NORMAL,
            context.getString(R.string.notif_channel_normal),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notif_channel_normal_desc)
            setSound(null, null)
            enableVibration(false)
        }

        // Канал для обычных уведомлений (тихий)
        val silentChannel = NotificationChannel(
            CHANNEL_ID_SILENT,
            context.getString(R.string.notif_channel_silent),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notif_channel_silent_desc)
            setSound(null, null)
            enableVibration(false)
        }

        manager.createNotificationChannels(
            listOf(urgentChannel, normalChannel, silentChannel)
        )
    }

    data class NotificationData(
        val petId: String,
        val petName: String,
        val title: String,
        val message: String,
        val isUrgent: Boolean,
        val status: PetLifeStatus
    )
}
