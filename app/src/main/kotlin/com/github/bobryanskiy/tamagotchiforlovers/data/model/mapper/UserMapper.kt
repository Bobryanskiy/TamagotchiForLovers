package com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper

import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.UserDto
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.User

/**
 * Firestore DTO → Domain model.
 *
 * Маппит все поля 1:1. Null-safety обеспечивается типами.
 */
fun UserDto.toDomain(): User = User(
    uid = uid,
    email = email,
    nickname = nickname,
    activePetId = activePetId,
    activePairId = activePairId,
    createdAt = createdAt
)

/**
 * Domain model → Firestore DTO (для upsert).
 *
 * Нужен для LinkAccountUseCase и других кейсов записи пользователя.
 */
fun User.toDto(): UserDto = UserDto(
    uid = uid,
    email = email,
    nickname = nickname,
    activePetId = activePetId,
    activePairId = activePairId,
    createdAt = createdAt
)
