package com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper

import com.github.bobryanskiy.tamagotchiforlovers.data.local.entity.PairEntity
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.InviteKeyDto
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.PairDto
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.PendingRequestDto
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.InviteKey
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PendingRequest
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pair
import com.google.firebase.Timestamp
import java.util.Date

// DTO → Entity
fun PairDto.toEntity(pairId: String): PairEntity {
    return PairEntity(
        id = pairId,
        name = name,
        userId1 = userId1,
        userId2 = userId2,
        currentPetId = currentPetId,
        status = status,
        inviteCode = inviteKey?.code,
        inviteExpiresAt = inviteKey?.expiresAt,
        requestGuestId = pendingRequest?.guestId,
        requestRequestedAt = pendingRequest?.requestedAt?.toDate()?.time,
        createdAt = createdAt,
        updatedAt = updatedAt,
        endedAt = endedAt?.toDate()?.time
    )
}

// Entity → DTO
fun PairEntity.toDto(): PairDto {
    return PairDto(
        name = name,
        userId1 = userId1,
        userId2 = userId2,
        currentPetId = currentPetId,
        status = status,
        inviteKey = if (inviteCode != null) InviteKeyDto(
            code = inviteCode,
            expiresAt = inviteExpiresAt ?: 0L
        ) else null,
        pendingRequest = if (requestGuestId != null) PendingRequestDto(
            guestId = requestGuestId,
            requestedAt = Timestamp(Date(requestRequestedAt?: 0L))
        ) else null,
        createdAt = createdAt,
        endedAt = Timestamp(Date(endedAt ?: 0L))
    )
}

fun PairDto.toDomain(pairId: String): Pair {
    val statusEnum = try {
        PairStatus.valueOf(status)
    } catch (e: IllegalArgumentException) {
        PairStatus.PENDING
    }

    val invite = inviteKey?.let {
        InviteKey(code = it.code, expiresAt = it.expiresAt)
    }

    val request = pendingRequest?.let {
        PendingRequest(guestId = it.guestId, requestedAt = it.requestedAt?.toDate()?.time ?: 0L)
    }

    return Pair(
        name = name,
        id = pairId,
        userId1 = userId1,
        userId2 = userId2,
        currentPetId = currentPetId,
        status = statusEnum,
        inviteKey = invite,
        pendingRequest = request,
        createdAt = createdAt,
        updatedAt = updatedAt,
        endedAt = endedAt?.toDate()?.time
    )
}

fun PairEntity.toDomain(): Pair {
    val statusEnum = try {
        PairStatus.valueOf(status)
    } catch (e: IllegalArgumentException) {
        PairStatus.PENDING
    }

    val invite = if (inviteCode != null && inviteExpiresAt != null) {
        InviteKey(code = inviteCode, expiresAt = inviteExpiresAt)
    } else null

    val request = if (requestGuestId != null) {
        PendingRequest(
            guestId = requestGuestId,
            requestedAt = requestRequestedAt ?: 0L
        )
    } else null

    return Pair(
        name = name,
        id = id,
        userId1 = userId1,
        userId2 = userId2,
        currentPetId = currentPetId,
        status = statusEnum,
        inviteKey = invite,
        pendingRequest = request,
        createdAt = createdAt,
        updatedAt = updatedAt,
        endedAt = endedAt
    )
}