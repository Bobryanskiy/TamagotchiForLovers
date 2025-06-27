package com.github.bobryanskiy.tamagotchiforlovers.data.notifications.receivers

import android.Manifest
import android.R
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationManager", "сработано")
        val pendingIntent: PendingIntent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("pendingIntent", PendingIntent::class.java)
        } else {
            intent.getParcelableExtra("pendingIntent")
        }
        // TODO : перед отправкой уведомления обновить данные из облака и проверить на актуальность уведомления
        sendReminderNotificationInternal(context, intent.getStringExtra("channel_name").toString(),
            intent.getStringExtra("title").toString(), intent.getStringExtra("text").toString(),
            intent.getIntExtra("request_code", -1), pendingIntent)
    }

    fun sendReminderNotificationInternal(context: Context, channelName: String, title: String, text: String, requestCode: Int, pendingIntent: PendingIntent?) {
        val builder = NotificationCompat.Builder(context, channelName)
            .setSmallIcon(R.drawable.ic_dialog_info) // Иконка уведомления
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(Notification.CATEGORY_REMINDER)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(requestCode, builder.build())
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val isScreenOn = pm.isInteractive // check if screen is on
            if (!isScreenOn) {
                val wl = pm.newWakeLock(
                    PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "tamagotchi:notificationLock"
                )
                wl.acquire(3000)
            }
        }
    }
}