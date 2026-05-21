package com.github.bobryanskiy.tamagotchiforlovers.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreSessionRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SessionRepository {

    private val petIdKey = stringPreferencesKey("active_pet_id")
    private val pairIdKey = stringPreferencesKey("active_pair_id")
    private val linkedKey = booleanPreferencesKey("account_linked")

    override fun getActivePetId(): String? = runBlocking { dataStore.data.first()[petIdKey] }
    override fun getActivePairId(): String? = runBlocking { dataStore.data.first()[pairIdKey] }
    override fun isAccountLinked(): Boolean = runBlocking { dataStore.data.first()[linkedKey] == true }
    override suspend fun saveActivePetId(id: String) { dataStore.edit { it[petIdKey] = id } }
    override suspend fun saveActivePairId(id: String) { dataStore.edit { it[pairIdKey] = id } }
    override suspend fun clearActivePetId() { dataStore.edit { it.remove(petIdKey) } }

    override suspend fun clearActivePairId() { dataStore.edit { it.remove(pairIdKey) } }
    override suspend fun setAccountLinked(linked: Boolean) { dataStore.edit { it[linkedKey] = linked } }

    override suspend fun clearAllSessionData() { dataStore.edit { it.clear() } }
}