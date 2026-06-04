package com.github.bobryanskiy.tamagotchiforlovers.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.RescheduleAlarmsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RescheduleAlarmsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val rescheduleAlarmsUseCase: RescheduleAlarmsUseCase,
    private val logger: AppLogger
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "RescheduleAlarmsWorker"
    }

    override suspend fun doWork(): Result {
        logger.d(TAG, "🔔 Starting alarm reschedule...")
        return try {
            val success = rescheduleAlarmsUseCase()
            if (success) {
                logger.d(TAG, "✅ Alarms rescheduled successfully")
                Result.success()
            } else {
                logger.w(TAG, "⚠️ Some alarms failed to reschedule, retrying")
                Result.retry()
            }
        } catch (e: Exception) {
            logger.e(TAG, "❌ Reschedule failed with exception", e)
            Result.retry()
        }
    }
}
