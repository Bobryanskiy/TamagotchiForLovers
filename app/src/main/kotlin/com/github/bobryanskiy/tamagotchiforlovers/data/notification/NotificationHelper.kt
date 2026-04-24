package com.github.bobryanskiy.tamagotchiforlovers.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAlertType
import com.github.bobryanskiy.tamagotchiforlovers.ui.MainActivity

object NotificationHelper {
    private const val CHANNEL_ID = "pet_alerts_channel"

    fun ensureChannelCreated(context: Context) {
        val channel = NotificationChannel(CHANNEL_ID, "Нужды питомца", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Критические уведомления о состоянии питомца"
            enableVibration(true)
            setShowBadge(true)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun showNotification(context: Context, petId: String, alertType: PetAlertType) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        ensureChannelCreated(context)

        val (title, text, icon) = when (alertType) {
            PetAlertType.HUNGER_WARNING -> Triple(
                "🥣 Голод: Внимание",
                "Питомец проголодался. Накорми его, чтобы восстановить силы.",
                R.drawable.pet
            )
            PetAlertType.HUNGER_CRITICAL -> Triple(
                "🥣 Голод: Критично",
                "Стремительно теряет энергию. Покорми немедленно.",
                R.drawable.pet
            )
            PetAlertType.HUNGER_DEATH -> Triple(
                "🥣 Голод: Потеря",
                "Питомец не выжил. Это важный опыт для будущего ухода.",
                R.drawable.pet
            )

            PetAlertType.ENERGY_WARNING -> Triple(
                "⚡ Энергия: Внимание",
                "Питомец устал и зевает. Отправь его отдыхать.",
                R.drawable.pet
            )
            PetAlertType.ENERGY_CRITICAL -> Triple(
                "⚡ Энергия: Критично",
                "Силы почти на нуле. Уложи спать, иначе упадёт.",
                R.drawable.pet
            )
            PetAlertType.ENERGY_COLLAPSE -> Triple(
                "⚡ Энергия: Отключение",
                "Питомец крепко спит. Не тревожь, пока не восстановится.",
                R.drawable.pet
            )

            PetAlertType.CLEANLINESS_WARNING -> Triple(
                "🛁 Чистота: Внимание",
                "Питомец испачкался. Помой его для комфорта.",
                R.drawable.pet
            )
            PetAlertType.CLEANLINESS_CRITICAL -> Triple(
                "🛁 Чистота: Критично",
                "Грязь зашкаливает. Высокий риск болезни!",
                R.drawable.pet
            )
            PetAlertType.CLEANLINESS_SICK -> Triple(
                "🛁 Чистота: Болезнь",
                "Питомец приболел из-за грязи. Требуется уход.",
                R.drawable.pet
            )

            PetAlertType.HAPPINESS_WARNING -> Triple(
                "🎈 Настроение: Внимание",
                "Питомец заскучал. Поиграй с ним или погладь.",
                R.drawable.pet
            )
            PetAlertType.HAPPINESS_CRITICAL -> Triple(
                "🎈 Настроение: Критично",
                "На грани стресса. Если не утешить — может уйти.",
                R.drawable.pet
            )
            PetAlertType.HAPPINESS_RUN_AWAY -> Triple(
                "🎈 Настроение: Уход",
                "Питомец убежал искать новых друзей. Жаль, но это его выбор.",
                R.drawable.pet
            )
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_pet_id", petId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        val notifId = petId.hashCode() xor alertType.ordinal
        NotificationManagerCompat.from(context).notify(notifId, notification)
    }
}