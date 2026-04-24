package com.github.bobryanskiy.tamagotchiforlovers.domain.model

data class User(
    val uid: String,
    val activePetId: String?,
    val activePairId: String?,
    val createdAt: Long
)