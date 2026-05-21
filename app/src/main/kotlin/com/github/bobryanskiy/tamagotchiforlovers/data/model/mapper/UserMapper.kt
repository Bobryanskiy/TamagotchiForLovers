package com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper

import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.UserDto
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.User

fun UserDto.toDomain() = User(
    uid = uid,
    activePetId = activePetId,
    activePairId = activePairId,
    createdAt = createdAt
)