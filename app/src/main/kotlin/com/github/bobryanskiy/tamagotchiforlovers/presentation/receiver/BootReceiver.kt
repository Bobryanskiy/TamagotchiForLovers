package com.github.bobryanskiy.tamagotchiforlovers.presentation.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.core.work.RescheduleAlarmsWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Receiver для восстановления алармов после перезагрузки устройства.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var logger: AppLogger

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            logger.d(TAG, "Ignoring action: ${intent.action}")
            return
        }

        logger.d(TAG, "Boot completed. Scheduling alarm reschedule work...")

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

        logger.d(TAG, "Work scheduled successfully")
    }
}