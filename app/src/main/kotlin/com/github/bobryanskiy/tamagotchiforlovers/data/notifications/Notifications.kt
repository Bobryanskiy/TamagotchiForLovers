package com.github.bobryanskiy.tamagotchiforlovers.data.notifications

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.TitleScreen
import com.github.bobryanskiy.tamagotchiforlovers.data.notifications.receivers.NotificationReceiver

class Notifications {
    var channelName: String = ""
    var actionName: String = ""
    var id: Int = -1
    var titleId: Int = -1
    var textId: Int = -1

    fun schedule(context: Context, delayInSeconds: Long) {
        val intentf = Intent(context, TitleScreen::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intentf, PendingIntent.FLAG_IMMUTABLE)
        Log.d("NotificationManager", "создано")
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("channel_name", channelName)
            putExtra("request_code", id)
            putExtra("action_name", actionName)
            putExtra("title", context.getString(titleId))
            putExtra("text", context.getString(textId))
            putExtra("pendingIntent", pendingIntent)
        }
        val receiverIntent = PendingIntent.getBroadcast(
            context,
            id,
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

    private fun getPendingIntent (context: Context): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            id,
            Intent(context, NotificationReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun cancel(context: Context, requestCode: Int) {
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(getPendingIntent(context))
    }

    companion object {
        private const val CHANNEL_NAME_PET_NEED = "tamagotchi.pet.need"
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
                description = context.getString(R.string.notification_channel_desc)
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

        val PetWantEat: Notifications
            get() {
                val petWantEat = Notifications()
                petWantEat.actionName = "tamagotchi.pet.need.eat"
                petWantEat.channelName = CHANNEL_NAME_PET_NEED
                petWantEat.id = 0
                petWantEat.titleId = R.string.pet_want_eat_title
                petWantEat.textId = R.string.pet_want_eat_text

                return petWantEat
            }
        val PetThirst: Notifications
            get() {
                val petThirst = Notifications()
                petThirst.actionName = "tamagotchi.pet.need.thirst"
                petThirst.channelName = CHANNEL_NAME_PET_NEED
                petThirst.id = 1
                petThirst.titleId = R.string.pet_thirst_title
                petThirst.textId = R.string.pet_thirst_text

                return petThirst
            }
//        object PetWantPlay {
//            private const val ACTION_NAME = "tamagotchi.pet.need.play"
//            private const val ID = 2
//            fun schedule(context: Context, delayInSeconds: Long, title: String, text: String, pendingIntent: PendingIntent) {
//                schedule(context, delayInSeconds, title, text, pendingIntent, this.ACTION_NAME, this.ID)
//            }
//            fun cancel(context: Context) {
//                cancel(context, this.ID)
//            }
//        }
//        object PetWantSleep {
//            private const val ACTION_NAME = "tamagotchi.pet.need.sleep"
//            private const val ID = 3
//            fun schedule(context: Context, delayInSeconds: Long, title: String, text: String, pendingIntent: PendingIntent) {
//                schedule(context, delayInSeconds, title, text, pendingIntent, this.ACTION_NAME, this.ID)
//            }
//            fun cancel(context: Context) {
//                cancel(context, this.ID)
//            }
//        }
    }
}