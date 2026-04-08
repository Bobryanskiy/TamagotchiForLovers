package com.github.bobryanskiy.tamagotchiforlovers.domain.model

data class Pair(
    val pairId: String = "",
    val userId1: String = "",
    val userId2: String = "",
    val petId: String = "",
    val status: PairStatus = PairStatus.SOLO,
    val createdAt: Long
)