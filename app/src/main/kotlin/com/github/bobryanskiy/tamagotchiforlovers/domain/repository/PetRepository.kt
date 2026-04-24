package com.github.bobryanskiy.tamagotchiforlovers.domain.repository

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetCriticalState
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    fun observePet(petId: String): Flow<Pet?>
    suspend fun applyAction(petId: String, action: PetAction): DomainResult<Unit>
    suspend fun createPet(name: String, ownerUserId: String): DomainResult<String>
    suspend fun updatePairId(petId: String, pairId: String)
    suspend fun updateCriticalState(petId: String, state: PetCriticalState): DomainResult<Unit>
    suspend fun abandonPet(petId: String): DomainResult<Unit>
}