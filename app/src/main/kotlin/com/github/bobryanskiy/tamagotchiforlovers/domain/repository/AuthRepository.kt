package com.github.bobryanskiy.tamagotchiforlovers.domain.repository

interface AuthRepository {
    suspend fun isLoggedIn(): Boolean
}