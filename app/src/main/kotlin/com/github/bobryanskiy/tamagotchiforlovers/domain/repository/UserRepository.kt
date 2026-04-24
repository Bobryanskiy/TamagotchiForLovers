package com.github.bobryanskiy.tamagotchiforlovers.domain.repository

import com.github.bobryanskiy.tamagotchiforlovers.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUserId(): String?
    suspend fun createUser(uid: String)
    suspend fun updateUserSession(uid: String, petId: String?, pairId: String?)
    fun observeUser(uid: String): Flow<User?>
}