package com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper

import com.github.bobryanskiy.tamagotchiforlovers.data.model.dto.PairDto
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.*

fun PairDto.toDomain(pairId: String): Pair {
    val statusEnum = runCatching { PairStatus.valueOf(status.uppercase()) }
        .getOrDefault(PairStatus.PENDING)

    return Pair(
        id = pairId,
        name = name,
        userId1 = userId1,
        userId2 = userId2,
        currentPetId = currentPetId,
        status = statusEnum,
        inviteKey = inviteKey?.let { InviteKey(it.code, it.expiresAt) },
        pendingRequest = pendingRequest?.let { PendingRequest(it.guestId, it.requestedAt) },
        createdAt = createdAt,
        endedAt = endedAt,
    )
}