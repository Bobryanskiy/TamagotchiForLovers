package com.github.bobryanskiy.tamagotchiforlovers.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.bobryanskiy.tamagotchiforlovers.data.sync.PetSyncManager
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncManager: PetSyncManager,
    private val authRepository: AuthRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (!authRepository.isLoggedIn()) return Result.success()

        return try {
            val success = syncManager.syncAll()
            if (success) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}