package com.github.bobryanskiy.tamagotchiforlovers.domain.repository

import com.github.bobryanskiy.tamagotchiforlovers.domain.result.UserResult

interface AuthRepository {
    fun getCurrentUserId(): String?
    fun getCurrentUserEmail(): String?
    fun isLoggedIn(): Boolean

    suspend fun signIn(email: String, password: String): UserResult<Unit>
    suspend fun signUp(email: String, password: String): UserResult<Unit>
    suspend fun signOut(): UserResult<Unit>
}
