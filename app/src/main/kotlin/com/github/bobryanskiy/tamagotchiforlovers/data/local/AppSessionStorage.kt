package com.github.bobryanskiy.tamagotchiforlovers.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSessionStorage @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val KEY_ACTIVE_PAIR_ID = stringPreferencesKey("active_pair_id")
        val KEY_ACTIVE_PET_ID = stringPreferencesKey("active_pet_id")
    }

    val activePairId: Flow<String?> = dataStore.data.map { it[KEY_ACTIVE_PAIR_ID] }
    val activePetId: Flow<String?> = dataStore.data.map { it[KEY_ACTIVE_PET_ID] }

    suspend fun setActivePairId(pairId: String?) {
        dataStore.edit { if (pairId != null) it[KEY_ACTIVE_PAIR_ID] = pairId else it.remove(KEY_ACTIVE_PAIR_ID) }
    }

    suspend fun setActivePetId(petId: String?) {
        dataStore.edit { if (petId != null) it[KEY_ACTIVE_PET_ID] = petId else it.remove(KEY_ACTIVE_PET_ID) }
    }

    suspend fun clearSession() {
        dataStore.edit {
            it.remove(KEY_ACTIVE_PAIR_ID)
            it.remove(KEY_ACTIVE_PET_ID)
        }
    }
}