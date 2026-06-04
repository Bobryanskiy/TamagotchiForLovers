package com.github.bobryanskiy.tamagotchiforlovers.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.github.bobryanskiy.tamagotchiforlovers.BuildConfig
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object Keys {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    }

    override fun observeNotificationsEnabled(): Flow<Boolean> =
        dataStore.data.map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }

    override fun observeSoundEnabled(): Flow<Boolean> =
        dataStore.data.map { it[Keys.SOUND_ENABLED] ?: true }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    override fun getAppVersion(): String = BuildConfig.VERSION_NAME
}
