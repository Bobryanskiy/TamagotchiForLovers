package com.github.bobryanskiy.tamagotchiforlovers.domain.repository

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeState
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PetResult
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    fun observePet(petId: String): Flow<Pet?>

    suspend fun getPetById(petId: String): PetResult<Pet?>

    suspend fun createPet(pet: Pet): PetResult<String>
    suspend fun savePet(pet: Pet): PetResult<Unit>

    suspend fun updateStats(
        petId: String,
        hunger: Int,
        energy: Int,
        cleanliness: Int,
        happiness: Int
    ): PetResult<Unit>

    suspend fun updateCriticalState(petId: String, state: PetLifeState): PetResult<Unit>

    suspend fun updatePairId(petId: String, pairId: String?): PetResult<Unit>

    suspend fun updatePetName(petId: String, name: String): PetResult<Unit>

    suspend fun deletePet(petId: String): PetResult<Unit>

    suspend fun getAllPetsByOwner(ownerId: String): PetResult<List<Pet>>
    suspend fun getAllActivePets(): PetResult<List<Pet>>
    suspend fun migrateOwnerUserId(oldOwnerId: String?, newOwnerId: String): PetResult<Unit>
    suspend fun syncPetsForOwner(ownerId: String): PetResult<List<Pet>>
}