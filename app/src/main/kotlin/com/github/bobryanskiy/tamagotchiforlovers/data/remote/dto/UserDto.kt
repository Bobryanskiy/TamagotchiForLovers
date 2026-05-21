package com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto

data class UserDto(
    val uid: String = "",
    val activePetId: String? = null,
    val activePairId: String? = null,
    val createdAt: Long = 0L
)