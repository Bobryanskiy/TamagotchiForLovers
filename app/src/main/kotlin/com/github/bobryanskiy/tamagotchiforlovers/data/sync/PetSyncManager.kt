package com.github.bobryanskiy.tamagotchiforlovers.data.sync

import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.data.local.datasource.LocalPetDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper.toDto
import com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper.toEntity
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.datasource.RemoteDataSource
import com.github.bobryanskiy.tamagotchiforlovers.di.IoDispatcher
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
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

    suspend fun syncPending(): Boolean = withContext(io) {
        val userId = authRepository.getCurrentUserId() ?: return@withContext false
        val pendingPets = local.getPendingPets()

        if (pendingPets.isEmpty()) return@withContext true

        Log.d(tag, "Syncing ${pendingPets.size} pending pets")
        var allSuccess = true

        for (entity in pendingPets) {
            try {
                val remoteDto = remote.getPet(entity.id)

                when {
                    remoteDto == null -> {
                        // Питомца нет в облаке — создаём
                        remote.upsertPet(entity.id, entity.toDto())
                        local.markSynced(entity.id)
                    }
                    else -> {
                        val localTime = entity.updatedAt
                        val remoteTime = remoteDto.stats?.updatedAt ?: 0L

                        when {
                            localTime > remoteTime -> {
                                // Локаль новее — пушим в облако
                                remote.upsertPet(entity.id, entity.toDto())
                                local.markSynced(entity.id)
                            }
                            remoteTime > localTime -> {
                                // Облако новее — пулим в локалку
                                val syncedEntity = remoteDto.toEntity(entity.id).copy(syncStatus = "SYNCED")
                                local.savePet(syncedEntity)
                            }
                            else -> {
                                // Версии совпадают — просто снимаем PENDING
                                local.markSynced(entity.id)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Sync failed for pet ${entity.id}", e)
                allSuccess = false
                // Не прерываем цикл — пытаемся синхронизировать остальных
            }
        }
        return@withContext allSuccess
    }
}