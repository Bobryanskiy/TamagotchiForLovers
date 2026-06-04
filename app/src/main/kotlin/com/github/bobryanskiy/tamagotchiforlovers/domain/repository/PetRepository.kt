package com.github.bobryanskiy.tamagotchiforlovers.domain.repository

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetLifeState
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PetResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.SyncResult
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    // ── Наблюдение ───────────────────────────────────────────────
    fun observePet(petId: String): Flow<Pet?>

    // ── Чтение ───────────────────────────────────────────────────
    suspend fun getPetById(petId: String): PetResult<Pet?>
    suspend fun getAllPetsByOwner(ownerId: String): PetResult<List<Pet>>
    suspend fun getAllActivePets(): PetResult<List<Pet>>

    // ── Мутации ──────────────────────────────────────────────────
    suspend fun createPet(pet: Pet): PetResult<String>
    suspend fun savePet(pet: Pet): PetResult<Unit>
    suspend fun updateStats(petId: String, hunger: Int, energy: Int, cleanliness: Int, happiness: Int): PetResult<Unit>
    suspend fun updateCriticalState(petId: String, state: PetLifeState): PetResult<Unit>
    suspend fun updatePairId(petId: String, pairId: String?): PetResult<Unit>
    suspend fun updatePetName(petId: String, name: String): PetResult<Unit>
    suspend fun deletePet(petId: String): PetResult<Unit>

    // ── Синхронизация (бизнес-уровень) ───────────────────────────
    suspend fun syncPetsForOwner(ownerId: String): PetResult<List<Pet>>
    suspend fun syncPetById(petId: String): PetResult<Pet>
    suspend fun syncAllPending(): SyncResult<Int>
    suspend fun migrateOwnerUserId(oldOwnerId: String?, newOwnerId: String): PetResult<Unit>
}
