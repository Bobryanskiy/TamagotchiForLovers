package com.github.bobryanskiy.tamagotchiforlovers.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.data.sync.PetSyncManager
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncManager: PetSyncManager,
    private val authRepository: AuthRepository,
    private val logger: AppLogger
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "SyncWorker"
    }

    override suspend fun doWork(): Result {
        if (!authRepository.isLoggedIn()) return Result.success()

        return try {
            when (val syncResult = syncManager.syncPending()) {
                is DomainResult.Success -> {
                    logger.d(TAG, "Synced ${syncResult.data} pets")
                    Result.success()
                }
                is DomainResult.Failure -> {
                    logger.w(TAG, "Sync failed: ${syncResult.error}")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            logger.e(TAG, "Sync exception", e)
            Result.retry()
        }
    }
}
