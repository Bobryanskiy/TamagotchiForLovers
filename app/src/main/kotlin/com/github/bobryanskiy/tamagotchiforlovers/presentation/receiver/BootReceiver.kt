package com.github.bobryanskiy.tamagotchiforlovers.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.*
import com.github.bobryanskiy.tamagotchiforlovers.core.work.RescheduleAlarmsWorker
import dagger.hilt.android.AndroidEntryPoint

/**
 * Receiver для восстановления алармов после перезагрузки устройства.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    private val tag = "BootReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            Log.d(tag, "Ignoring action: ${intent.action}")
            return
        }

        Log.d(tag, "Boot completed. Scheduling alarm reschedule work...")

        val workRequest = OneTimeWorkRequestBuilder<RescheduleAlarmsWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .addTag("reschedule_alarms")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "reschedule_alarms",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        Log.d(tag, "Work scheduled successfully")
    }
}