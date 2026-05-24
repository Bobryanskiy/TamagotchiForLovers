package com.github.bobryanskiy.tamagotchiforlovers.domain.repository

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetPair
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PendingRequest
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PairResult
import kotlinx.coroutines.flow.Flow

interface PairRepository {
    // ── Наблюдение ───────────────────────────────────────────────
    fun observePair(pairId: String): Flow<PetPair?>
    fun observePendingRequests(pairId: String): Flow<List<PendingRequest>>

    // ── Чтение ───────────────────────────────────────────────────
    suspend fun getPair(pairId: String): PairResult<PetPair?>  // ← теперь Result

    // ── Создание и управление ────────────────────────────────────
    suspend fun createPair(creatorId: String, pairName: String, petId: String): PairResult<String>
    suspend fun generateInviteKey(pairId: String): PairResult<String>
    suspend fun findPairByInviteKey(inviteKey: String): PairResult<PetPair>
    suspend fun updatePairName(pairId: String, newName: String): PairResult<Unit>

    // ── Мультиплеер ──────────────────────────────────────────────
    suspend fun requestJoin(pairId: String, guestId: String): PairResult<Unit>
    suspend fun acceptJoinRequest(pairId: String, guestId: String, callerId: String): PairResult<Unit>
    suspend fun rejectJoinRequest(pairId: String, guestId: String, callerId: String): PairResult<Unit>
    suspend fun leaveSession(pairId: String, userId: String): PairResult<Unit>
    suspend fun endSession(pairId: String, callerId: String): PairResult<Unit>
    suspend fun kickPartner(pairId: String, callerId: String): PairResult<Unit>
}