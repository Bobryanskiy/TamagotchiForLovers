package com.github.bobryanskiy.tamagotchiforlovers.data.repository

import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.data.local.datasource.LocalPetDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper.toDomain
import com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper.toDto
import com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper.toEntity
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.datasource.RemoteDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.PetDto
import com.github.bobryanskiy.tamagotchiforlovers.data.sync.PetSyncManager
import com.github.bobryanskiy.tamagotchiforlovers.di.IoDispatcher
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeState
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PetResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.SyncResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.NameLimits
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepositoryImpl @Inject constructor(
    private val local: LocalPetDataSource,
    private val remote: RemoteDataSource,
    private val authRepository: AuthRepository,
    private val petSyncManager: PetSyncManager,
    private val clock: Clock,
    private val logger: AppLogger,
    @param:IoDispatcher private val io: CoroutineDispatcher
) : PetRepository {

    private val scope = CoroutineScope(SupervisorJob() + io)
    private val syncScope = CoroutineScope(SupervisorJob() + io)

    companion object {
        private const val TAG = "PetRepo"
    }

    override fun observePet(petId: String): Flow<Pet?> = flow {
        // 1. Сначала эмитим текущее значение из Room (мгновенно)
        local.getPet(petId)?.let { emit(it.toDomain()) }

        // 2. Используем coroutineScope — он отменится вместе с Flow
        coroutineScope {
            val syncJob = launch {
                if (!authRepository.isLoggedIn()) {
                    logger.d(TAG, "User not authenticated, skipping Firestore sync for $petId")
                    return@launch
                }

                try {
                    remote.observePet(petId).collect { dto ->
                        if (dto == null) {
                            // Пет удалён в облаке — удаляем локально
                            local.deletePet(petId)
                            logger.d(TAG, "🗑️ Pet deleted in cloud, removed locally: $petId")
                            return@collect
                        }

                        val remoteEntity = dto.toEntity(petId).copy(syncStatus = "SYNCED")
                        val localEntity = local.getPet(petId)

                        // Применяем только если remote новее
                        if (localEntity == null || remoteEntity.updatedAt > localEntity.updatedAt) {
                            local.savePet(remoteEntity)
                            logger.d(TAG, "🔄 Synced from Firestore: ${remoteEntity.updatedAt} > ${localEntity?.updatedAt}")
                        } else {
                            logger.d(TAG, "⏭️ Local is newer, skipping: ${localEntity.updatedAt} >= ${remoteEntity.updatedAt}")
                        }
                    }
                } catch (e: CancellationException) {
                    throw e  // Нормальное поведение при отмене
                } catch (e: Exception) {
                    logger.e(TAG, "Firestore observe error for $petId", e)
                }
            }

            // 3. Наблюдаем ТОЛЬКО за Room — он наш single source of truth
            // Когда Firestore запишет в Room, Room автоматически эмитит новое значение
            local.observePet(petId)
                .mapNotNull { it?.toDomain() }
                .distinctUntilChanged()  // Избегаем дублей
                .collect { pet -> emit(pet) }
        }
    }.flowOn(io)

    override suspend fun getPetById(petId: String): PetResult<Pet?> = execute {
        local.getPet(petId)?.toDomain()
    }

    override suspend fun createPet(pet: Pet): PetResult<String> = execute {
        val entity = pet.toEntity().copy(syncStatus = "PENDING")
        val dto = pet.toEntity().toDto()

        local.savePet(entity)

        syncScope.launch {
            try {
                remote.upsertPet(pet.id, dto)
                local.markSynced(pet.id)
            } catch (e: Exception) {
                logger.e(TAG, "Sync failed for ${pet.id}", e)
            }
        }
        pet.id
    }

    override suspend fun savePet(pet: Pet): PetResult<Unit> = execute {
        val entity = pet.toEntity().copy(syncStatus = "PENDING", updatedAt = clock.currentTimeMillis())
        local.savePet(entity)

        syncScope.launch {
            try {
                remote.upsertPet(pet.id, entity.toDto())
                local.markSynced(pet.id)
            } catch (e: Exception) {
                logger.w(TAG, "Sync failed for ${pet.id}", e)
            }
        }
    }

    override suspend fun updateStats(
        petId: String,
        hunger: Int,
        energy: Int,
        cleanliness: Int,
        happiness: Int
    ): PetResult<Unit> = execute {
        val now = clock.currentTimeMillis()
        local.updateStats(petId, hunger, energy, cleanliness, happiness, now)
        local.markPending(petId)

        syncScope.launch {
            try {
                remote.updatePetStats(petId, hunger, energy, cleanliness, happiness, now)
                local.markSynced(petId)
            } catch (e: Exception) {
                logger.w(TAG, "Remote updateStats failed", e)
            }
        }
    }

    override suspend fun updateCriticalState(petId: String, state: PetLifeState): PetResult<Unit> = execute {
        val now = clock.currentTimeMillis()
        local.updateLifeState(
            petId,
            state.status.name,
            state.decayMultiplier,
            state.recoveryEndTime,
            now
        )
        local.markPending(petId)

        syncScope.launch {
            try {
                remote.updatePetLifeState(
                    petId,
                    state.status.name,
                    state.decayMultiplier,
                    state.recoveryEndTime,
                    now
                )
                local.markSynced(petId)
            } catch (e: Exception) {
                logger.w(TAG, "Remote updateLifeState failed", e)
            }
        }
    }

    override suspend fun updatePairId(petId: String, pairId: String?): PetResult<Unit> = execute {
        val now = clock.currentTimeMillis()
        local.updatePairId(petId, pairId, now)
        local.markPending(petId)

        syncScope.launch {
            try {
                remote.updatePetPairId(petId, pairId, now)
                local.markSynced(petId)
            } catch (e: Exception) {
                logger.w(TAG, "Remote updatePairId failed", e)
            }
        }
    }

    override suspend fun updatePetName(petId: String, name: String): PetResult<Unit> = execute {
        val trimmed = name.trim()
        if (trimmed.length !in NameLimits.PET_NAME_MIN..NameLimits.PET_NAME_MAX) {
            throw IllegalArgumentException("Invalid pet name length")
        }

        val now = clock.currentTimeMillis()
        local.updateName(petId, trimmed, now)
        local.markPending(petId)

        syncScope.launch {
            try {
                remote.updatePetName(petId, trimmed, now)
                local.markSynced(petId)
            } catch (e: Exception) {
                logger.w(TAG, "Remote updatePetName failed", e)
            }
        }
    }

    override suspend fun getAllPetsByOwner(ownerId: String): PetResult<List<Pet>> = execute {
        local.getPetsByOwner(ownerId).map { it.toDomain() }
    }

    override suspend fun getAllActivePets(): PetResult<List<Pet>> = execute {
        local.getAllActivePets().map { it.toDomain() }
    }

    override suspend fun migrateOwnerUserId(oldOwnerId: String?, newOwnerId: String): PetResult<Unit> = execute {
        val now = clock.currentTimeMillis()
        local.migrateOwnerUserId(oldOwnerId, newOwnerId, now)
        local.markAllPending()

        syncScope.launch {
            try {
                remote.batchMigrateOwnerUserId(oldOwnerId, newOwnerId)
            } catch (e: Exception) {
                logger.w(TAG, "Remote batchMigrate failed", e)
            }
        }
    }

    override suspend fun syncPetsForOwner(ownerId: String): PetResult<List<Pet>> = execute {
        val pairs: List<Pair<String, PetDto>> = remote.getPetsByOwner(ownerId)

        val entities = pairs.map { (id, dto) ->
            dto.toEntity(id).copy(syncStatus = "SYNCED")
        }

        for (entity in entities) {
            local.savePet(entity)
        }

        entities.map { it.toDomain() }
    }

    /**
     * Синхронизирует одного питомца с сервером.
     * Использует стратегию last-write-wins на основе updatedAt.
     */
    override suspend fun syncPetById(petId: String): PetResult<Pet> = execute {
        val localEntity = local.getPet(petId)
            ?: throw IllegalStateException("Pet $petId not found in local DB")

        val remoteDto = runCatching { remote.getPet(petId) }.getOrNull()

        val syncedEntity = when {
            // Питомца нет в облаке — создаём
            remoteDto == null -> {
                logger.d(TAG, "🆕 Pet $petId not in cloud, creating")
                remote.upsertPet(petId, localEntity.toDto())
                localEntity.copy(syncStatus = "SYNCED")
            }
            // Локальная версия новее — пушим в облако
            localEntity.updatedAt > (remoteDto.stats?.updatedAt ?: 0L) -> {
                logger.d(TAG, "⬆️ Local newer, pushing to cloud")
                remote.upsertPet(petId, localEntity.toDto())
                localEntity.copy(syncStatus = "SYNCED")
            }
            // Remote версия новее — пулим в локалку
            (remoteDto.stats?.updatedAt ?: 0L) > localEntity.updatedAt -> {
                logger.d(TAG, "⬇️ Remote newer, pulling to local")
                remoteDto.toEntity(petId).copy(syncStatus = "SYNCED")
            }
            // Версии совпадают — просто помечаем как SYNCED
            else -> {
                logger.d(TAG, "✅ Versions match, marking SYNCED")
                localEntity.copy(syncStatus = "SYNCED")
            }
        }

        local.savePet(syncedEntity)
        syncedEntity.toDomain()
    }

    override suspend fun syncAllPending(): SyncResult<Int> {
        return petSyncManager.syncPending()
    }

    override suspend fun deletePet(petId: String): PetResult<Unit> = withContext(io) {
        try {
            runCatching { remote.deletePet(petId) }
                .onFailure { e -> logger.w(TAG, "Remote delete failed (ignored)", e) }
            local.deletePet(petId)
            DomainResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.e(TAG, "Delete pet failed", e)
            DomainResult.Failure(PetError.Database)
        }
    }

    private suspend fun <T> execute(block: suspend () -> T): PetResult<T> = withContext(io) {
        try {
            DomainResult.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.e(TAG, "CRITICAL DB ERROR", e)
            DomainResult.Failure(PetError.Database)
        }
    }
}