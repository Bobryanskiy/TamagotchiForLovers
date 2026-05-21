package com.github.bobryanskiy.tamagotchiforlovers.domain.repository

interface SessionRepository {
    fun getActivePetId(): String?
    fun getActivePairId(): String?
    suspend fun saveActivePetId(id: String)
    suspend fun saveActivePairId(id: String)
    suspend fun clearActivePetId()
    suspend fun clearActivePairId()
    suspend fun setAccountLinked(linked: Boolean)
    fun isAccountLinked(): Boolean
    suspend fun clearAllSessionData()
}