package com.github.bobryanskiy.tamagotchiforlovers.core.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.RescheduleAlarmsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker для перепланирования алармов после перезагрузки.
 * Используется Hilt для внедрения зависимостей.
 */
@HiltWorker
class RescheduleAlarmsWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val rescheduleAlarmsUseCase: RescheduleAlarmsUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val success = rescheduleAlarmsUseCase.invoke()
            if (success) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}