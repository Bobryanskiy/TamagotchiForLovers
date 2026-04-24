package com.github.bobryanskiy.tamagotchiforlovers.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.*
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.AlertSchedule
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAlertType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class HybridNotificationScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context
) : NotificationScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val workManager = WorkManager.getInstance(context)

    override suspend fun scheduleAlerts(schedules: List<AlertSchedule>) {
        if (canScheduleExact()) {
            scheduleWithAlarmManager(schedules)
        } else {
            scheduleWithWorkManager(schedules)
        }
    }

    private fun scheduleWithAlarmManager(schedules: List<AlertSchedule>) {
        for (schedule in schedules) {
            val requestCode = generateRequestCode(schedule.petId, schedule.alertType)
            val intent = Intent(context, PetAlertReceiver::class.java).apply {
                putExtra(PetAlertReceiver.EXTRA_PET_ID, schedule.petId)
                putExtra(PetAlertReceiver.EXTRA_ALERT_TYPE, schedule.alertType.name)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(schedule.triggerAtMillis, pendingIntent),
                pendingIntent
            )
        }
    }

    private fun scheduleWithWorkManager(schedules: List<AlertSchedule>) {
        for (schedule in schedules) {
            val delayMinutes = max(0, (schedule.triggerAtMillis - System.currentTimeMillis()) / 60_000L)

            val data = Data.Builder()
                .putString(PetAlertWorker.KEY_PET_ID, schedule.petId)
                .putString(PetAlertWorker.KEY_ALERT_TYPE, schedule.alertType.name)
                .build()

            val request = OneTimeWorkRequestBuilder<PetAlertWorker>()
                .setInputData(data)
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .addTag("pet_${schedule.petId}")
                .build()

            workManager.enqueueUniqueWork(
                "alert_${schedule.petId}_${schedule.alertType}",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    override fun cancelAlertsForPet(petId: String) {
        PetAlertType.entries.forEach { type ->
            val requestCode = generateRequestCode(petId, type)
            val intent = Intent(context, PetAlertReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }

        workManager.cancelAllWorkByTag("pet_$petId")
    }

    private fun generateRequestCode(petId: String, type: PetAlertType): Int {
        return (petId.hashCode() xor (type.ordinal shl 16)) and 0x7FFFFFFF
    }

    private fun canScheduleExact(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
}