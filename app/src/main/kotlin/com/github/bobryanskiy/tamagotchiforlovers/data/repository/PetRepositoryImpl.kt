package com.github.bobryanskiy.tamagotchiforlovers.data.repository

import com.github.bobryanskiy.tamagotchiforlovers.data.local.datasource.RoomLocalPetDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper.toDomain
import com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper.toDto
import com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper.toEntity
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.datasource.RemoteDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.PetDto
import com.github.bobryanskiy.tamagotchiforlovers.di.IoDispatcher
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeState
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CalculateTimeDecayUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
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
import kotlin.collections.map

@Singleton
class PetRepositoryImpl @Inject constructor(
    private val local: RoomLocalPetDataSource,
    private val remote: RemoteDataSource,
    private val authRepository: AuthRepository,
    private val timeDecayUseCase: CalculateTimeDecayUseCase,
    private val clock: Clock,
    @param:IoDispatcher private val io: CoroutineDispatcher
) : PetRepository {


    private val syncScope = CoroutineScope(SupervisorJob() + io)

    override fun observePet(petId: String): Flow<Pet?> = flow {
        try {
            local.getPet(petId)?.let { emit(it.toDomain()) }

            val flows = mutableListOf<Flow<Pet?>>()

            flows.add(local.observePet(petId).mapNotNull { it?.toDomain() })

            if (authRepository.isLoggedIn()) {
                flows.add(
                    remote.observePet(petId).mapNotNull { dto ->
                        if (dto == null) {
                            syncScope.launch { local.deletePet(petId) }
                            return@mapNotNull null
                        }

                        val entity = dto.toEntity(petId).copy(syncStatus = "SYNCED")

                        val localEntity = local.getPet(petId)
                        if (localEntity != null && localEntity.updatedAt >= entity.updatedAt) {
                            return@mapNotNull localEntity.toDomain()
                        }

                        local.savePet(entity)
                        entity.toDomain()
                    }
                )
            } else {
                android.util.Log.d("PetRepo", "User not authenticated. Skipping Firestore listen for pet $petId")
            }

            merge(*flows.toTypedArray())
                .distinctUntilChanged()
                .collect { pet ->
                    val now = clock.currentTimeMillis()
                    pet?.let { nonNullPet ->
                        val updatedPet = timeDecayUseCase(nonNullPet, currentTime = now)

                        if (updatedPet.stats.updatedAt != pet.stats.updatedAt) {
                            syncScope.launch {
                                local.savePet(entity = updatedPet.toEntity().copy(syncStatus = "PENDING"))
                            }
                        }

                        emit(value = updatedPet)
                    } ?: emit(value = null)
                }

        } catch (e: Exception) {
            android.util.Log.e("PetRepo", "Error in observePet (likely auth issue): ${e.message}")
        }
    }.flowOn(io)

    override suspend fun getPetById(petId: String): DomainResult<Pet?> = execute {
        local.getPet(petId)?.toDomain()
    }

    override suspend fun createPet(pet: Pet): DomainResult<String> = execute {
        val entity = pet.toEntity().copy(syncStatus = "PENDING")
        val dto = pet.toEntity().toDto()

        local.savePet(entity)

        syncScope.launch {
            try {
                remote.upsertPet(pet.id, dto)
                local.markSynced(pet.id)
            } catch (e: Exception) {
                android.util.Log.e("PetRepository", "Sync failed for ${pet.id}", e)
            }
        }
        pet.id
    }

    override suspend fun savePet(pet: Pet): DomainResult<Unit> = execute {
        val entity = pet.toEntity().copy(syncStatus = "PENDING", updatedAt = clock.currentTimeMillis())
        local.savePet(entity)

        syncScope.launch {
            try {
                remote.upsertPet(pet.id, entity.toDto())
                local.markSynced(pet.id)
            } catch (_: Exception) {}
        }
    }

    override suspend fun updateStats(
        petId: String,
        hunger: Int, energy: Int, cleanliness: Int, happiness: Int
    ): DomainResult<Unit> = execute {
        val now = clock.currentTimeMillis()
        local.updateStats(petId, hunger, energy, cleanliness, happiness, now)

        syncScope.launch {
            try {
                remote.updatePetStats(petId, hunger, energy, cleanliness, happiness, now)
            } catch (_: Exception) {}
        }
    }

    override suspend fun updateCriticalState(petId: String, state: PetLifeState): DomainResult<Unit> = execute {
        val now = clock.currentTimeMillis()
        local.updateLifeState(
            petId,
            state.status.name,
            state.isActionsBlocked,
            state.decayMultiplier,
            state.recoveryEndTime,
            now
        )

        syncScope.launch {
            try {
                remote.updatePetLifeState(
                    petId,
                    state.status.name,
                    state.isActionsBlocked,
                    state.decayMultiplier,
                    state.recoveryEndTime,
                    now
                )
            } catch (_: Exception) {}
        }
    }

    override suspend fun updatePairId(petId: String, pairId: String?): DomainResult<Unit> = execute {
        val now = clock.currentTimeMillis()
        local.updatePairId(petId, pairId, now)

        syncScope.launch {
            try {
                remote.updatePetPairId(petId, pairId, now)
            } catch (_: Exception) {}
        }
    }

    override suspend fun updatePetName(petId: String, name: String): DomainResult<Unit> = execute {
        val now = clock.currentTimeMillis()
        local.updateName(petId, name, now)

        syncScope.launch {
            try {
                remote.updatePetName(petId, name, now)
            } catch (_: Exception) {}
        }
    }

    override suspend fun getAllPetsByOwner(ownerId: String): DomainResult<List<Pet>> = execute {
        local.getPetsByOwner(ownerId).map { it.toDomain() }
    }

    override suspend fun getAllActivePets(): DomainResult<List<Pet>> = execute {
        local.getAllActivePets().map { it.toDomain() }
    }

    override suspend fun migrateOwnerUserId(oldOwnerId: String?, newOwnerId: String): DomainResult<Unit> = execute {
        val now = clock.currentTimeMillis()
        local.migrateOwnerUserId(oldOwnerId, newOwnerId, now)
        local.markAllPending()

        syncScope.launch {
            try {
                remote.batchMigrateOwnerUserId(oldOwnerId, newOwnerId)
            } catch (_: Exception) {}
        }
    }

    override suspend fun syncPetsForOwner(ownerId: String): DomainResult<List<Pet>> {
        return execute {
            val pairs: List<Pair<String, PetDto>> = remote.getPetsByOwner(ownerId)

            val entities: List<com.github.bobryanskiy.tamagotchiforlovers.data.local.entity.PetEntity> =
                pairs.map { (id, dto) ->
                    dto.toEntity(id).copy(
                        syncStatus = "SYNCED"
                    )
                }

            for (entity in entities) {
                local.savePet(entity)
            }

            val pets: List<Pet> = entities.map { it.toDomain() }

            pets
        }
    }

    override suspend fun deletePet(petId: String): DomainResult<Unit> = withContext(io) {
        try {
            runCatching {
                remote.deletePet(petId)
            }.onFailure { e ->
                android.util.Log.w("PetRepository", "Remote delete failed (ignored): $e")
            }
            local.deletePet(petId)

            DomainResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            DomainResult.Failure(PetError.Database)
        }
    }

    private suspend fun <T> execute(block: suspend () -> T): DomainResult<T> = withContext(io) {
        try {
            DomainResult.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            android.util.Log.e("PET_REPO_ERROR", "CRITICAL DB ERROR", e)
            e.printStackTrace()
            DomainResult.Failure(PetError.Database)
        }
    }
}