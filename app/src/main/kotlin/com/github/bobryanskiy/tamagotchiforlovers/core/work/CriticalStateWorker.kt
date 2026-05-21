package com.github.bobryanskiy.tamagotchiforlovers.core.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CheckAndNotifyPetsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CriticalStateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val checkAndNotifyPetsUseCase: CheckAndNotifyPetsUseCase
) : CoroutineWorker(context, params) {

    private val TAG = "CriticalStateWorker"

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting critical state check for all pets")

        return try {
            val result = checkAndNotifyPetsUseCase.invoke()

            when (result) {
                true -> {
                    Log.d(TAG, "Critical state check completed successfully")
                    Result.success()
                }
                false -> {
                    Log.w(TAG, "Critical state check completed with errors")
                    Result.success()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Critical error during critical state check", e)
            Result.retry()
        }
    }
}