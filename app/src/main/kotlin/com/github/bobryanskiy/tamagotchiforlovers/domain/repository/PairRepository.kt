package com.github.bobryanskiy.tamagotchiforlovers.domain.repository

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pair
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PendingRequest
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow

interface PairRepository {
    fun observePair(pairId: String): Flow<Pair?>
    fun observePendingRequests(pairId: String): Flow<List<PendingRequest>>

    suspend fun createPair(creatorId: String, pairName: String, petId: String): DomainResult<String>
    suspend fun generateInviteKey(pairId: String): DomainResult<String>
    suspend fun findPairByInviteKey(inviteKey: String): DomainResult<Pair>
    suspend fun getPair(pairId: String): Pair?
    suspend fun updatePairName(pairId: String, newName: String): DomainResult<Unit>

    suspend fun requestJoin(pairId: String, guestId: String): DomainResult<Unit>
    suspend fun acceptJoinRequest(pairId: String, guestId: String, callerId: String): DomainResult<Unit>

    suspend fun rejectJoinRequest(pairId: String, guestId: String, callerId: String): DomainResult<Unit>
    suspend fun leaveSession(pairId: String, userId: String): DomainResult<Unit>
    suspend fun endSession(pairId: String, callerId: String): DomainResult<Unit>
    suspend fun kickPartner(pairId: String, callerId: String): DomainResult<Unit>
}