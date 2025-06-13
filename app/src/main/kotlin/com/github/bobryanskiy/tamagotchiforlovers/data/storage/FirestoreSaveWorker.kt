package com.github.bobryanskiy.tamagotchiforlovers.data.storage

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirestoreSaveWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    companion object {
        const val KEY_PET_ID = "pet_id"
        const val KEY_HUNGER = "hunger"
        const val KEY_TIREDNESS = "tiredness"
    }

    private val firestore by lazy { Firebase.firestore }

    override suspend fun doWork(): Result {
        val petId = inputData.getString(KEY_PET_ID) ?: return Result.failure()
        val hunger = inputData.getInt(KEY_HUNGER, 0)
        val tiredness = inputData.getInt(KEY_TIREDNESS, 0)

        val petRef = firestore.collection("pets").document(petId)

        return try {
            petRef.update(mapOf(
                "hunger" to hunger,
                "tiredness" to tiredness
            )).await()
            Result.success()
        } catch (e: Exception) {
            // Повтор попытки через WorkManager
            Result.retry()
        }
    }
}