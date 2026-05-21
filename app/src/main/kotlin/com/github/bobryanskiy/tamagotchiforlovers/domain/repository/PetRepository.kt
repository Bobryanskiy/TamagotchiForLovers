package com.github.bobryanskiy.tamagotchiforlovers.domain.repository

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeState
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    fun observePet(petId: String): Flow<Pet?>

    suspend fun getPetById(petId: String): DomainResult<Pet?>

    suspend fun createPet(pet: Pet): DomainResult<String>
    suspend fun savePet(pet: Pet): DomainResult<Unit>

    suspend fun updateStats(
        petId: String,
        hunger: Int,
        energy: Int,
        cleanliness: Int,
        happiness: Int
    ): DomainResult<Unit>

    suspend fun updateCriticalState(petId: String, state: PetLifeState): DomainResult<Unit>

    suspend fun updatePairId(petId: String, pairId: String?): DomainResult<Unit>

    suspend fun updatePetName(petId: String, name: String): DomainResult<Unit>

    suspend fun deletePet(petId: String): DomainResult<Unit>

    suspend fun getAllPetsByOwner(ownerId: String): DomainResult<List<Pet>>
    suspend fun getAllActivePets(): DomainResult<List<Pet>>
    suspend fun migrateOwnerUserId(oldOwnerId: String?, newOwnerId: String): DomainResult<Unit>
    suspend fun syncPetsForOwner(ownerId: String): DomainResult<List<Pet>>
}