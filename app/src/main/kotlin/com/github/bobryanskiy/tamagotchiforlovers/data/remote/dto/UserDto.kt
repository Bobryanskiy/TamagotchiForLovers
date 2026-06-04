package com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto

import com.google.firebase.firestore.PropertyName

data class UserDto(
    @get:PropertyName("uid") @set:PropertyName("uid") var uid: String = "",
    @get:PropertyName("email") @set:PropertyName("email") var email: String? = null,
    @get:PropertyName("nickname") @set:PropertyName("nickname") var nickname: String? = null,
    @get:PropertyName("active_pet_id") @set:PropertyName("active_pet_id") var activePetId: String? = null,
    @get:PropertyName("active_pair_id") @set:PropertyName("active_pair_id") var activePairId: String? = null,
    @get:PropertyName("created_at") @set:PropertyName("created_at") var createdAt: Long = 0L
) {
    constructor() : this("", null, null, null, null, 0L)
}
