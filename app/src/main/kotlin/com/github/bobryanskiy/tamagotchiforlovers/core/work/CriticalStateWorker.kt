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

    private val tag = "CriticalStateWorker"

    override suspend fun doWork(): Result {
        Log.d(tag, "Starting critical state check for all pets")

        return try {
            val success = checkAndNotifyPetsUseCase.invoke()
            if (success) {
                Result.success()
            } else {
                // Если что-то пошло не так — пробуем снова позже
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(tag, "Critical error during check", e)
            Result.retry()
        }
    }
}