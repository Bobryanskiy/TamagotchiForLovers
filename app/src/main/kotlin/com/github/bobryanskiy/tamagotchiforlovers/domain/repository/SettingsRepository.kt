package com.github.bobryanskiy.tamagotchiforlovers.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeNotificationsEnabled(): Flow<Boolean>
    fun observeSoundEnabled(): Flow<Boolean>

    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setSoundEnabled(enabled: Boolean)

    fun getAppVersion(): String
}
