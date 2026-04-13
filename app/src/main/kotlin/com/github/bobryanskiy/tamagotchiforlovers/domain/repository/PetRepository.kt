package com.github.bobryanskiy.tamagotchiforlovers.domain.repository

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    fun observePet(petId: String): Flow<Pet?>
    suspend fun applyPetAction(petId: String, action: PetAction): Result<Unit>
    suspend fun createPet(name: String, ownerUserId: String): Result<String>
    suspend fun deletePet(petId: String): Result<Unit>
}