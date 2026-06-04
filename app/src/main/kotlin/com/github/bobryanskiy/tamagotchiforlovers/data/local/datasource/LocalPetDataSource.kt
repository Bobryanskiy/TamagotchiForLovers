package com.github.bobryanskiy.tamagotchiforlovers.data.local.datasource

import com.github.bobryanskiy.tamagotchiforlovers.data.local.entity.PetEntity
import kotlinx.coroutines.flow.Flow

interface LocalPetDataSource {
    fun observePet(petId: String): Flow<PetEntity?>
    suspend fun getPet(petId: String): PetEntity?
    suspend fun savePet(entity: PetEntity)
    suspend fun getPendingPets(): List<PetEntity>
    suspend fun markSynced(petId: String)
    suspend fun updateStats(petId: String, hunger: Int, energy: Int, cleanliness: Int, happiness: Int, timestamp: Long)
    suspend fun updateLifeState(petId: String, status: String, deathCause: String?, timestamp: Long)
    suspend fun updatePairId(petId: String, pairId: String?, timestamp: Long)
    suspend fun updateName(petId: String, name: String, timestamp: Long)
    suspend fun deletePet(petId: String)
    suspend fun getPetsByOwner(ownerId: String): List<PetEntity>
    suspend fun getAllActivePets(): List<PetEntity>
    suspend fun migrateOwnerUserId(oldOwnerId: String?, newOwnerId: String, timestamp: Long)
    suspend fun markAllPending()
    suspend fun markPending(petId: String)
}
