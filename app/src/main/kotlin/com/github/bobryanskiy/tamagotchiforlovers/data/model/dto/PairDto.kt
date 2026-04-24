package com.github.bobryanskiy.tamagotchiforlovers.data.model.dto

data class PairDto(
    val name: String = "",
    val userId1: String = "",
    val userId2: String? = null,
    val currentPetId: String = "",
    val status: String = "PENDING",
    val inviteKey: InviteKeyDto? = null,
    val pendingRequest: PendingRequestDto? = null,
    val createdAt: Long = 0L,
    val endedAt: Long? = null
)

data class InviteKeyDto(
    val code: String = "",
    val expiresAt: Long = 0L
)
data class PendingRequestDto(
    val guestId: String = "",
    val requestedAt: Long = 0L
)