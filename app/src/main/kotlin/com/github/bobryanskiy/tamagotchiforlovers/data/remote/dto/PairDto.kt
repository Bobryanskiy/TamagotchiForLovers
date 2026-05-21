package com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

data class PairDto(
    @PropertyName("name") val name: String = "",
    @PropertyName("user_id_1") val userId1: String = "",
    @PropertyName("user_id_2") val userId2: String? = null,
    @PropertyName("current_pet_id") val currentPetId: String = "",
    @PropertyName("status") val status: String = "PENDING",
    @PropertyName("invite_key") val inviteKey: InviteKeyDto? = null,
    @PropertyName("pending_request") val pendingRequest: PendingRequestDto? = null,
    @PropertyName("created_at") val createdAt: Long = 0L,
    @PropertyName("updated_at") val updatedAt: Long = 0L,
    @PropertyName("ended_at") @ServerTimestamp val endedAt: Long? = null
)

data class InviteKeyDto(
    @PropertyName("code") val code: String = "",
    @PropertyName("expires_at") val expiresAt: Long = 0L
)

data class PendingRequestDto(
    @PropertyName("guest_id") val guestId: String = "",
    @PropertyName("requested_at") @ServerTimestamp val requestedAt: Long? = null
)