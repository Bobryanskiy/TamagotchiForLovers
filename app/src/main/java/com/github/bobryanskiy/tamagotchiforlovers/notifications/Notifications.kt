package com.github.bobryanskiy.tamagotchiforlovers.notifications

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.util.Log

class Notifications {
    companion object {
        const val CHANNEL_NAME_PET_NEED = "tamagotchi.pet.need"

        fun createNotificationChannels(context: Context) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val myAudioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            // Канал для напоминаний о нуждах питомца
            val petWantChannel = NotificationChannel(
                CHANNEL_NAME_PET_NEED,
                "Нужды питомца",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Ваш питомец что-то хочет"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(soundUri, myAudioAttributes)
                setShowBadge(true)
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(petWantChannel)
        }

        private fun schedule(context: Context, delayInSeconds: Long, title: String, text: String, pendingIntent: PendingIntent, actionName: String, requestCode: Int) {
            Log.d("NotificationManager", "создано")
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("channel_name", CHANNEL_NAME_PET_NEED)
                putExtra("request_code", requestCode)
                putExtra("action_name", actionName)
                putExtra("title", title)
                putExtra("text", text)
                putExtra("pendingIntent", pendingIntent)
            }
            val receiverIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val triggerTime = delayInSeconds

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                receiverIntent
            )
        }

        private fun getPendingIntent (context: Context, requestCode: Int): PendingIntent {
            return PendingIntent.getBroadcast(
                context,
                requestCode,
                Intent(context, NotificationReceiver::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun cancel(context: Context, requestCode: Int) {
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(getPendingIntent(context, requestCode))
        }
    }

    object PetWantEat {
        private const val ACTION_NAME = "tamagotchi.pet.need.eat"
        private const val ID = 0
        fun schedule(context: Context, delayInSeconds: Long, title: String, text: String, pendingIntent: PendingIntent) {
            schedule(context, delayInSeconds, title, text, pendingIntent, this.ACTION_NAME, this.ID)
        }
        fun cancel(context: Context) {
            cancel(context, this.ID)
        }
    }
    object PetThirst {
        private const val ACTION_NAME = "tamagotchi.pet.need.thirst"
        private const val ID = 1
        fun schedule(context: Context, delayInSeconds: Long, title: String, text: String, pendingIntent: PendingIntent) {
            schedule(context, delayInSeconds, title, text, pendingIntent, this.ACTION_NAME, this.ID)
        }
        fun cancel(context: Context) {
            cancel(context, this.ID)
        }
    }
    object PetWantPlay {
        private const val ACTION_NAME = "tamagotchi.pet.need.play"
        private const val ID = 2
        fun schedule(context: Context, delayInSeconds: Long, title: String, text: String, pendingIntent: PendingIntent) {
            schedule(context, delayInSeconds, title, text, pendingIntent, this.ACTION_NAME, this.ID)
        }
        fun cancel(context: Context) {
            cancel(context, this.ID)
        }
    }
    object PetWantSleep {
        private const val ACTION_NAME = "tamagotchi.pet.need.sleep"
        private const val ID = 3
        fun schedule(context: Context, delayInSeconds: Long, title: String, text: String, pendingIntent: PendingIntent) {
            schedule(context, delayInSeconds, title, text, pendingIntent, this.ACTION_NAME, this.ID)
        }
        fun cancel(context: Context) {
            cancel(context, this.ID)
        }
    }
}