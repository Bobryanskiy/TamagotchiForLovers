package com.github.bobryanskiy.tamagotchiforlovers.data.notification

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAlertType
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class PetAlertWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_PET_ID = "pet_id"
        const val KEY_ALERT_TYPE = "alert_type"
    }

    override suspend fun doWork(): Result {
        val petId = inputData.getString(KEY_PET_ID)
        val alertTypeName = inputData.getString(KEY_ALERT_TYPE)

        if (petId == null || alertTypeName == null) {
            Log.e("TAMAGOTCHI", "PetAlertWorker: Missing inputs")
            return Result.failure()
        }

        val alertType = try {
            PetAlertType.valueOf(alertTypeName)
        } catch (e: IllegalArgumentException) {
            Log.e("TAMAGOTCHI", "PetAlertWorker: Invalid type", e)
            return Result.failure()
        }

        Log.d("TAMAGOTCHI", "PetAlertWorker: Triggering notification for $petId ($alertType)")

        NotificationHelper.showNotification(applicationContext, petId, alertType)

        return Result.success()
    }
}