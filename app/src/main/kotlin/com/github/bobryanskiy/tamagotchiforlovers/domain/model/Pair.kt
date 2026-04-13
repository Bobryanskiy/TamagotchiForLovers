package com.github.bobryanskiy.tamagotchiforlovers.domain.model

import kotlin.time.Instant

data class Pair(
    val id: String,
    val name: String?,
    val userId1: String,
    val userId2: String?,
    val currentPetId: String,
    val status: PairStatus,
    val inviteKey: InviteKey?,
    val pendingRequest: PendingRequest? = null,
    val createdAt: Instant,
    val endedAt: Long
)

data class InviteKey(
    val code: String,
    val expiresAt: Long
)

data class PendingRequest(
    val guestId: String,
    val requestedAt: Long
)