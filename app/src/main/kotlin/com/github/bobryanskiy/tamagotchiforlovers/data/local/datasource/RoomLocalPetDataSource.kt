package com.github.bobryanskiy.tamagotchiforlovers.data.local.datasource

import com.github.bobryanskiy.tamagotchiforlovers.data.local.dao.PetDao
import com.github.bobryanskiy.tamagotchiforlovers.data.local.entity.PetEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomLocalPetDataSource @Inject constructor(
    private val petDao: PetDao
) : LocalPetDataSource {
    override fun observePet(petId: String): Flow<PetEntity?> = petDao.observePet(petId)
    override suspend fun getPet(petId: String): PetEntity? = petDao.getPet(petId)
    override suspend fun savePet(entity: PetEntity) = petDao.savePet(entity)
    override suspend fun getPendingPets(): List<PetEntity> = petDao.getPendingPets()
    override suspend fun markSynced(petId: String) = petDao.markSynced(petId)
    override suspend fun updateStats(petId: String, hunger: Int, energy: Int, cleanliness: Int, happiness: Int, timestamp: Long) =
        petDao.updateStats(petId, hunger, energy, cleanliness, happiness, timestamp)
    override suspend fun updateLifeState(petId: String, status: String, isBlocked: Boolean, multiplier: Float, recoveryTime: Long?, timestamp: Long) =
        petDao.updateLifeState(petId, status, isBlocked, multiplier, recoveryTime, timestamp)
    override suspend fun updatePairId(petId: String, pairId: String?, timestamp: Long) =
        petDao.updatePairId(petId, pairId, timestamp)
    override suspend fun updateName(petId: String, name: String, timestamp: Long) =
        petDao.updateName(petId, name, timestamp)
    override suspend fun deletePet(petId: String) = petDao.deletePet(petId)
    override suspend fun getPetsByOwner(ownerId: String): List<PetEntity> = petDao.getPetsByOwner(ownerId)
    override suspend fun getAllActivePets(): List<PetEntity> = petDao.getAllActivePets()
    override suspend fun migrateOwnerUserId(oldOwnerId: String?, newOwnerId: String, timestamp: Long) =
        petDao.migrateOwnerUserId(oldOwnerId, newOwnerId, timestamp)
    override suspend fun markAllPending() = petDao.markAllPending()
}