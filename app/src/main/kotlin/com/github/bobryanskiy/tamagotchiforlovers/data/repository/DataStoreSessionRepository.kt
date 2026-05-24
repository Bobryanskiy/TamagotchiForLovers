package com.github.bobryanskiy.tamagotchiforlovers.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreSessionRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SessionRepository {

    private object Keys {
        val petId = stringPreferencesKey("active_pet_id")
        val pairId = stringPreferencesKey("active_pair_id")
        val pairStatus = stringPreferencesKey("pair_status")
        val linked = booleanPreferencesKey("account_linked")
    }

    // ── Наблюдение ───────────────────────────────────────────────────
    override fun observeActivePetId(): Flow<String?> = dataStore.data.map { it[Keys.petId] }
    override fun observeActivePairId(): Flow<String?> = dataStore.data.map { it[Keys.pairId] }
    override fun observeActivePairStatus(): Flow<String?> = dataStore.data.map { it[Keys.pairStatus] }

    // ── Чтение (suspend!) ────────────────────────────────────────────
    override suspend fun getActivePetId(): String? = dataStore.data.first()[Keys.petId]
    override suspend fun getActivePairId(): String? = dataStore.data.first()[Keys.pairId]
    override suspend fun getActivePairStatus(): String? = dataStore.data.first()[Keys.pairStatus]
    override suspend fun isAccountLinked(): Boolean = dataStore.data.first()[Keys.linked] == true

    // ── Запись ───────────────────────────────────────────────────────
    override suspend fun saveActivePetId(id: String) { dataStore.edit { it[Keys.petId] = id } }
    override suspend fun saveActivePairId(id: String) { dataStore.edit { it[Keys.pairId] = id } }
    override suspend fun savePairStatus(status: String) { dataStore.edit { it[Keys.pairStatus] = status } }
    override suspend fun setAccountLinked(linked: Boolean) { dataStore.edit { it[Keys.linked] = linked } }

    // ── Очистка ──────────────────────────────────────────────────────
    override suspend fun clearActivePairId() { dataStore.edit { it.remove(Keys.pairId) } }
    override suspend fun clearPairStatus() { dataStore.edit { it.remove(Keys.pairStatus) } }
    override suspend fun clearActivePetId() { dataStore.edit { it.remove(Keys.petId) } }
    override suspend fun clearAllSessionData() { dataStore.edit { it.clear() } }
}