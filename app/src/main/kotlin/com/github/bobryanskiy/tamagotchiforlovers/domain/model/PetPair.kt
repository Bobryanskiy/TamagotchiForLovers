package com.github.bobryanskiy.tamagotchiforlovers.domain.model

import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock

data class PetPair(
    val id: String,
    val name: String,
    val userId1: String,
    val userId2: String?,
    val currentPetId: String,
    val status: PairStatus,
    val inviteKey: InviteKey?,
    val pendingRequest: PendingRequest?,
    val createdAt: Long?,
    val updatedAt: Long?,
    val endedAt: Long?
) {
    val isActive: Boolean
        get() = status == PairStatus.ACTIVE

    val canAcceptRequests: Boolean
        get() = status == PairStatus.PENDING && userId2 == null
}

data class InviteKey(
    val code: String,
    val expiresAt: Long
) {
    fun isValid(clock: Clock): Boolean = clock.currentTimeMillis() < expiresAt
}

data class PendingRequest(
    val guestId: String,
    val requestedAt: Long
)

enum class PairStatus {
    PENDING,
    ACTIVE,
    ENDED
}
