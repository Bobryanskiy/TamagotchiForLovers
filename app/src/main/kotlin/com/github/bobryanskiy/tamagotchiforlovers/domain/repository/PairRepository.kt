package com.github.bobryanskiy.tamagotchiforlovers.domain.repository

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pair
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow

interface PairRepository {
    fun observePair(pairId: String): Flow<Pair?>
    suspend fun createPair(creatorId: String, pairName: String, petId: String): DomainResult<String>
    suspend fun generateInviteKey(pairId: String): DomainResult<String>
    suspend fun findPairByInviteKey(inviteKey: String): DomainResult<Pair>
    suspend fun requestJoin(pairId: String, guestId: String): DomainResult<Unit>
    suspend fun acceptPlayer(pairId: String, guestId: String): DomainResult<Unit>
    suspend fun leaveSession(pairId: String, guestId: String): DomainResult<Unit>
    suspend fun kickPlayer(pairId: String, kickedId: String): DomainResult<Unit>
    suspend fun endSession(pairId: String): DomainResult<Unit>

    suspend fun getCurrentUserId(): String?
}