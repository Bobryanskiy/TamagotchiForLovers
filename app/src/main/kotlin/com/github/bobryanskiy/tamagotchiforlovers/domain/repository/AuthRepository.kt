package com.github.bobryanskiy.tamagotchiforlovers.domain.repository

import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult

interface AuthRepository {
    fun getCurrentUserId(): String?
    fun getCurrentUserEmail(): String?
    fun isLoggedIn(): Boolean
    suspend fun signIn(email: String, password: String): DomainResult<Unit>
    suspend fun signUp(email: String, password: String): DomainResult<Unit>
    suspend fun signOut(): DomainResult<Unit>
}