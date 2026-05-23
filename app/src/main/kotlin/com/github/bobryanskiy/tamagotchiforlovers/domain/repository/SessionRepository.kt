package com.github.bobryanskiy.tamagotchiforlovers.domain.repository

interface SessionRepository {
    fun getActivePetId(): String?
    fun getActivePairId(): String?
    fun getActivePairStatus(): String?

    suspend fun saveActivePetId(id: String)
    suspend fun saveActivePairId(id: String)
    suspend fun savePairStatus(status: String)
    suspend fun clearActivePetId()
    suspend fun clearActivePairId()
    suspend fun setAccountLinked(linked: Boolean)
    fun isAccountLinked(): Boolean
    suspend fun clearAllSessionData()
    suspend fun clearPairStatus()
}