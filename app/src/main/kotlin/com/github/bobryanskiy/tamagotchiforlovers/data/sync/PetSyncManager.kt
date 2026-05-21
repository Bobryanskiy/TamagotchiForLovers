package com.github.bobryanskiy.tamagotchiforlovers.data.sync

import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.data.local.datasource.LocalPetDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.local.entity.PetEntity
import com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper.toDto
import com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper.toEntity
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.datasource.RemoteDataSource
import com.github.bobryanskiy.tamagotchiforlovers.di.IoDispatcher
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetSyncManager @Inject constructor(
    private val local: LocalPetDataSource,
    private val remote: RemoteDataSource,
    private val authRepository: AuthRepository,
    @param:IoDispatcher private val io: CoroutineDispatcher
) {

    private val tag = "PetSyncManager"

    suspend fun syncAll(): Boolean = withContext(io) {
        val userId = authRepository.getCurrentUserId() ?: return@withContext false

        val pendingIds = local.getPendingPets().map { it.id }.toSet()
        if (pendingIds.isEmpty()) return@withContext true

        Log.d(tag, "Syncing ${pendingIds.size} pending pets")

        val results = pendingIds.map { petId ->
            async { syncSinglePet(petId, userId) }
        }.awaitAll()

        val success = results.all { it }
        Log.d(tag, "Sync finished. Success: $success")
        success
    }

    private suspend fun syncSinglePet(petId: String, userId: String): Boolean {
        val localEntity = local.getPet(petId) ?: return false
        val localUpdatedAt = localEntity.updatedAt

        return try {
            val cloudDto = remote.getPet(petId)

            if (cloudDto == null) {
                Log.d(tag, "Cloud is empty for $petId. Pushing local version as new.")
                pushToCloud(localEntity, userId)
                return true
            }

            val cloudUpdatedAt = cloudDto.stats?.updatedAt?.time ?: 0L

            if (cloudUpdatedAt > localUpdatedAt) {
                Log.d(tag, "Cloud is newer ($cloudUpdatedAt > $localUpdatedAt). Pulling.")
                local.savePet(cloudDto.toEntity(petId).copy(syncStatus = "SYNCED"))
                return true
            }  else {
                Log.d(tag, "Local is newer or equal ($localUpdatedAt >= $cloudUpdatedAt). Pushing.")
                pushToCloud(localEntity, userId)
                return true
            }
        } catch (e: Exception) {
            Log.e(tag, "Sync failed for $petId", e)
            false
        }
    }
    private suspend fun pushToCloud(entity: PetEntity, userId: String) {
        val dto = entity.toDto().copy(
            profile = entity.toDto().profile?.copy(ownerUserId = userId)
        )
        remote.upsertPet(entity.id, dto)
        local.markSynced(entity.id)
    }
}
