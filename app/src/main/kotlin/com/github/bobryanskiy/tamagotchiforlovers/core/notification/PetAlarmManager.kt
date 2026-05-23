package com.github.bobryanskiy.tamagotchiforlovers.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeState
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeStatus
import com.github.bobryanskiy.tamagotchiforlovers.presentation.receiver.CriticalStateReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetAlarmManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val tag = "PetAlarmManager"

    fun scheduleCheck(petId: String, lifeState: PetLifeState) {
        if (lifeState.isTerminal()) {
            cancelCheck(petId)
            Log.d(tag, "Cancelled alarms for terminal pet: $petId")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.w(tag, "No permission for exact alarms. Skipping schedule for $petId")
            return
        }

        val interval = if (lifeState.status == PetLifeStatus.SICK) {
            15 * 60 * 1000L
        } else {
            60 * 60 * 1000L
        }
        val triggerTime = System.currentTimeMillis() + interval

        val intent = Intent(context, CriticalStateReceiver::class.java).apply {
            putExtra("PET_ID", petId)
            // Можно добавить тип уведомления, если понадобится
            // putExtra("NOTIFICATION_TYPE", "CRITICAL_STATE")
        }

        val requestCode = "${petId}_critical".hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 5. Планируем аларм
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d(tag, "Scheduled alarm for $petId at ${triggerTime} (interval: ${interval/1000}s)")
        } catch (e: SecurityException) {
            Log.e(tag, "Failed to schedule alarm for $petId: ${e.message}")
        }
    }

    fun cancelCheck(petId: String) {
        val intent = Intent(context, CriticalStateReceiver::class.java)
        val requestCode = "${petId}_critical".hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            Log.d(tag, "Cancelled alarm for $petId")
        }
    }
}