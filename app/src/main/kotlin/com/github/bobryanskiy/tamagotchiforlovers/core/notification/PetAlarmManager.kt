package com.github.bobryanskiy.tamagotchiforlovers.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.github.bobryanskiy.tamagotchiforlovers.BuildConfig
import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeState
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeStatus
import com.github.bobryanskiy.tamagotchiforlovers.presentation.receiver.CriticalStateReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetAlarmManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val logger: AppLogger
) {
    companion object {
        private const val TAG = "PetAlarmManager"
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleCheck(petId: String, lifeState: PetLifeState) {
        if (lifeState.isTerminal()) {
            cancelCheck(petId)
            return
        }

        // Android 12+: нужна явная проверка permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                logger.w(TAG, "No exact alarm permission for $petId. Request via Settings.")
                // Можно показать диалог: "Разрешите точные уведомления в настройках"
                return
            }
        }

        val interval = if (BuildConfig.DEBUG) {
            30 * 1000L  // 30 секунд вместо 15-60 минут
        } else {
            when (lifeState.status) {
                PetLifeStatus.SICK -> 15 * 60 * 1000L
                PetLifeStatus.COLLAPSED -> 5 * 60 * 1000L
                else -> 60 * 60 * 1000L
            }
        }

        val triggerTime = System.currentTimeMillis() + interval

        val intent = Intent(context, CriticalStateReceiver::class.java).apply {
            putExtra("PET_ID", petId)
            action = "ACTION_CHECK_PET_$petId"  // ✅ уникальный action для каждого пета
        }

        val requestCode = petId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            logger.d(TAG, "Scheduled alarm for $petId at $triggerTime")
        } catch (e: SecurityException) {
            logger.e(TAG, "Failed to schedule alarm for $petId", e)
        }
    }

    fun cancelCheck(petId: String) {
        val intent = Intent(context, CriticalStateReceiver::class.java).apply {
            action = "ACTION_CHECK_PET_$petId"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            petId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            logger.d(TAG, "Cancelled alarm for $petId")
        }
    }
}