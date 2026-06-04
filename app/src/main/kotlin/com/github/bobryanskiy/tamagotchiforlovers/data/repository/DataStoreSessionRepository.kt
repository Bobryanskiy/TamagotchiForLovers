package com.github.bobryanskiy.tamagotchiforlovers.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Loadable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreSessionRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SessionRepository {

    private object Keys {
        val ACTIVE_PET_ID = stringPreferencesKey("active_pet_id")
        val ACTIVE_PAIR_ID = stringPreferencesKey("active_pair_id")
        val PAIR_STATUS = stringPreferencesKey("pair_status")
        val IS_LINKED = booleanPreferencesKey("account_linked")
    }

    // ── Наблюдение ───────────────────────────────────────────────────
    override fun observeActivePetId(): Flow<Loadable<String?>> =
        dataStore.data
            .map<Preferences, Loadable<String?>> { prefs ->
                Loadable.Loaded(prefs[Keys.ACTIVE_PET_ID])
            }
            .onStart { emit(Loadable.Loading) }

    override fun observeActivePairId(): Flow<Loadable<String?>> =
        dataStore.data
            .map<Preferences, Loadable<String?>> { prefs ->
                Loadable.Loaded(prefs[Keys.ACTIVE_PAIR_ID])
            }
            .onStart { emit(Loadable.Loading) }


    override fun observeActivePairStatus(): Flow<String?> = dataStore.data.map { it[Keys.PAIR_STATUS] }

    // ── Чтение (suspend!) ────────────────────────────────────────────
    override suspend fun getActivePetId(): String? = dataStore.data.first()[Keys.ACTIVE_PET_ID]
    override suspend fun getActivePairId(): String? = dataStore.data.first()[Keys.ACTIVE_PAIR_ID]
    override suspend fun getActivePairStatus(): String? = dataStore.data.first()[Keys.PAIR_STATUS]
    override suspend fun isAccountLinked(): Boolean = dataStore.data.first()[Keys.IS_LINKED] == true

    // ── Запись ───────────────────────────────────────────────────────
    override suspend fun saveActivePetId(id: String) { dataStore.edit { it[Keys.ACTIVE_PET_ID] = id } }
    override suspend fun saveActivePairId(id: String) { dataStore.edit { it[Keys.ACTIVE_PAIR_ID] = id } }
    override suspend fun savePairStatus(status: String) { dataStore.edit { it[Keys.PAIR_STATUS] = status } }
    override suspend fun setAccountLinked(linked: Boolean) { dataStore.edit { it[Keys.IS_LINKED] = linked } }

    // ── Очистка ──────────────────────────────────────────────────────
    override suspend fun clearActivePairId() { dataStore.edit { it.remove(Keys.ACTIVE_PAIR_ID) } }
    override suspend fun clearPairStatus() { dataStore.edit { it.remove(Keys.PAIR_STATUS) } }
    override suspend fun clearActivePetId() { dataStore.edit { it.remove(Keys.ACTIVE_PET_ID) } }
    override suspend fun clearAllSessionData() { dataStore.edit { it.clear() } }
}
