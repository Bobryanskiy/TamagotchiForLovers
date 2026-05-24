package com.github.bobryanskiy.tamagotchiforlovers.data.remote.datasource

import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.PairDto
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.PetDto
import kotlinx.coroutines.flow.Flow

interface RemoteDataSource {
    // Pet
    fun observePet(petId: String): Flow<PetDto?>
    suspend fun getPet(petId: String): PetDto?
    suspend fun upsertPet(petId: String, dto: PetDto)
    suspend fun updatePetStats(petId: String, hunger: Int, energy: Int, cleanliness: Int, happiness: Int, updatedAt: Long)
    suspend fun updatePetLifeState(petId: String, status: String, multiplier: Float, recoveryTime: Long?, updatedAt: Long)
    suspend fun updatePetPairId(petId: String, pairId: String?, updatedAt: Long)
    suspend fun updatePetName(petId: String, name: String, updatedAt: Long)
    suspend fun deletePet(petId: String)
    suspend fun getPetsByOwner(ownerId: String): List<Pair<String, PetDto>>
    suspend fun batchMigrateOwnerUserId(oldOwnerId: String?, newOwnerId: String)

    // Pair
    fun observePair(pairId: String): Flow<PairDto?>
    suspend fun getPair(pairId: String): PairDto?
    suspend fun upsertPair(pairId: String, dto: PairDto)
    suspend fun updatePairStatus(pairId: String, status: String, updatedAt: Long)
    suspend fun updatePairUserId2(pairId: String, userId2: String?, updatedAt: Long)
    suspend fun deletePair(pairId: String)
    suspend fun findPairByInviteKey(inviteKey: String): PairDto?
    suspend fun requestJoin(pairId: String, guestId: String)
    suspend fun acceptJoinRequest(pairId: String, guestId: String)
    suspend fun rejectJoinRequest(pairId: String, guestId: String)
    suspend fun leaveSession(pairId: String, userId: String)
    suspend fun endSession(pairId: String, callerId: String)
    suspend fun kickPartner(pairId: String, callerId: String)
    suspend fun generateInviteKey(pairId: String, code: String, expiresAt: Long)
    fun observePendingRequests(pairId: String): Flow<List<Map<String, Any?>>>
}