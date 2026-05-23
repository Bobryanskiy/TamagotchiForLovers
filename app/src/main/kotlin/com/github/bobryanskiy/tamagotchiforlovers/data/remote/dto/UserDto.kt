package com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto

import com.google.firebase.firestore.PropertyName

data class UserDto(
    @PropertyName("uid") val uid: String = "",
    @PropertyName("active_pet_id") val activePetId: String? = null,
    @PropertyName("active_pair_id") val activePairId: String? = null,
    @PropertyName("created_at") val createdAt: Long = 0L
)