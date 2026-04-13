package com.github.bobryanskiy.tamagotchiforlovers.domain.repository

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pair
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import kotlinx.coroutines.flow.Flow

interface PairRepository {
    fun observePair(pairId: String): Flow<Pair>
    suspend fun createPair(ownerId: String, pet: Pet): Result<Unit>
    suspend fun generateInviteKey(pairId: String): Result<String>
    suspend fun requestJoin(pairId: String, guestId: String): Result<Unit>
    suspend fun acceptPlayer(pairId: String, guestId: String): Result<Unit>
    suspend fun deletePair(pairId: String): Result<Unit>
}