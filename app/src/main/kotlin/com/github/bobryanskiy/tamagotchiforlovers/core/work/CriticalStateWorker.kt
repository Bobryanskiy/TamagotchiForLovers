package com.github.bobryanskiy.tamagotchiforlovers.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CriticalStateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val checkAndNotifyPetsUseCase: CheckAndNotifyPetsWorkerUseCase,
    private val logger: AppLogger
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "CriticalStateWorker"
    }

    override suspend fun doWork(): Result {
        logger.d(TAG, "Starting critical state check for all pets")
        return try {
            val success = checkAndNotifyPetsUseCase()
            if (success) Result.success() else Result.retry()
        } catch (e: Exception) {
            logger.e(TAG, "Critical error during check", e)
            Result.retry()
        }
    }
}