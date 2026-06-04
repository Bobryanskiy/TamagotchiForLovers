package com.github.bobryanskiy.tamagotchiforlovers.data.sync

import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.data.local.datasource.LocalPetDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper.toDto
import com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper.toEntity
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.datasource.RemoteDataSource
import com.github.bobryanskiy.tamagotchiforlovers.di.IoDispatcher
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.SyncError
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetSyncManager @Inject constructor(
    private val local: LocalPetDataSource,
    private val remote: RemoteDataSource,
    private val authRepository: AuthRepository,
    private val logger: AppLogger,
    @param:IoDispatcher private val io: CoroutineDispatcher
) {
    companion object {
        private const val TAG = "PetSyncManager"
        private const val MAX_RETRIES = 3
        private const val INITIAL_RETRY_DELAY_MS = 1_000L
        private const val MAX_RETRY_DELAY_MS = 10_000L
    }

    suspend fun syncPending(): DomainResult<Int, SyncError> = withContext(io) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            logger.w(TAG, "User not authenticated, skipping sync")
            return@withContext DomainResult.Success(0)
        }

        val pendingPets = local.getPendingPets()
        if (pendingPets.isEmpty()) {
            logger.d(TAG, "No pending pets to sync")
            return@withContext DomainResult.Success(0)
        }

        logger.d(TAG, "Syncing ${pendingPets.size} pending pets")

        var successCount = 0
        var failureCount = 0

        for (entity in pendingPets) {
            when (val result = syncSinglePet(entity.id, entity.updatedAt)) {
                is DomainResult.Success -> successCount++
                is DomainResult.Failure -> {
                    failureCount++
                    logger.w(TAG, "Sync failed for pet ${entity.id}: ${result.error}")
                }
            }
        }

        logger.d(TAG, "Sync completed: $successCount success, $failureCount failed")

        if (failureCount == 0) {
            DomainResult.Success(successCount)
        } else {
            DomainResult.Failure(SyncError.PartialSuccess(successCount, failureCount))
        }
    }

    private suspend fun syncSinglePet(petId: String, localTimestamp: Long): DomainResult<Unit, SyncError> {
        var retryDelay = INITIAL_RETRY_DELAY_MS

        repeat(MAX_RETRIES) { attempt ->
            try {
                val remoteDto = remote.getPet(petId)

                when {
                    remoteDto == null -> {
                        logger.d(TAG, "🆕 Pet $petId not in cloud, creating")
                        val localEntity = local.getPet(petId) ?: throw IllegalStateException("Pet disappeared")
                        remote.upsertPet(petId, localEntity.toDto())
                        local.markSynced(petId)
                    }
                    localTimestamp > (remoteDto.stats?.updatedAt ?: 0L) -> {
                        logger.d(TAG, "⬆️ Local newer, pushing")
                        val localEntity = local.getPet(petId) ?: throw IllegalStateException("Pet disappeared")
                        remote.upsertPet(petId, localEntity.toDto())
                        local.markSynced(petId)
                    }
                    (remoteDto.stats?.updatedAt ?: 0L) > localTimestamp -> {
                        logger.d(TAG, "⬇️ Remote newer, pulling")
                        val syncedEntity = remoteDto.toEntity(petId).copy(syncStatus = "SYNCED")
                        local.savePet(syncedEntity)
                    }
                    else -> {
                        logger.d(TAG, "✅ Versions match, marking SYNCED")
                        local.markSynced(petId)
                    }
                }

                return DomainResult.Success(Unit)

            } catch (e: Exception) {
                logger.e(TAG, "Sync attempt ${attempt + 1}/$MAX_RETRIES failed for $petId", e)
                if (attempt < MAX_RETRIES - 1) {
                    delay(retryDelay)
                    retryDelay = (retryDelay * 2).coerceAtMost(MAX_RETRY_DELAY_MS)
                }
            }
        }

        return DomainResult.Failure(SyncError.NetworkError)
    }
}
