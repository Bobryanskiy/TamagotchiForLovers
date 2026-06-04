package com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

data class PairDto(
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("user_id_1") @set:PropertyName("user_id_1") var userId1: String = "",
    @get:PropertyName("user_id_2") @set:PropertyName("user_id_2") var userId2: String? = null,
    @get:PropertyName("current_pet_id") @set:PropertyName("current_pet_id") var currentPetId: String = "",
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "PENDING",
    @get:PropertyName("invite_key") @set:PropertyName("invite_key") var inviteKey: InviteKeyDto? = null,
    @get:PropertyName("pending_request") @set:PropertyName("pending_request") var pendingRequest: PendingRequestDto? = null,
    @get:PropertyName("created_at") @set:PropertyName("created_at") var createdAt: Long? = null,
    @get:PropertyName("updated_at") @set:PropertyName("updated_at") var updatedAt: Long? = null,
    @get:PropertyName("ended_at") @set:PropertyName("ended_at") @ServerTimestamp var endedAt: Timestamp? = null
) {
    constructor() : this("", "", null, "", "PENDING", null, null, null, null, null)
}

data class InviteKeyDto(
    @get:PropertyName("code") @set:PropertyName("code") var code: String = "",
    @get:PropertyName("expires_at") @set:PropertyName("expires_at") var expiresAt: Long = 0L
) {
    constructor() : this("", 0L)
}

data class PendingRequestDto(
    @get:PropertyName("guest_id") @set:PropertyName("guest_id") var guestId: String = "",
    @get:PropertyName("requested_at") @set:PropertyName("requested_at") @ServerTimestamp var requestedAt: Timestamp? = null
) {
    constructor() : this("", null)
}
