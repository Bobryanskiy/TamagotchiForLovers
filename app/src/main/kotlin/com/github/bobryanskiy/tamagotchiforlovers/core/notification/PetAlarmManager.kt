package com.github.bobryanskiy.tamagotchiforlovers.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.receiver.CriticalStateReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetAlarmManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleCheck(petId: String, lifeState: PetLifeState) {
        if (lifeState.isTerminal()) {
            cancelCheck(petId)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return
        }

        val interval = if (lifeState.status.name == "SICK") 15 * 60 * 1000L else 60 * 60 * 1000L
        val triggerTime = System.currentTimeMillis() + interval

        val intent = Intent(context, CriticalStateReceiver::class.java).apply {
            putExtra("PET_ID", petId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, petId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun cancelCheck(petId: String) {
        val intent = Intent(context, CriticalStateReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, petId.hashCode(), intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }
}