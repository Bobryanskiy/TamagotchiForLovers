package com.github.bobryanskiy.tamagotchiforlovers.domain.repository

import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Loadable
import kotlinx.coroutines.flow.Flow

/**
 * Контракт для работы с пользовательской сессией (активный pet, pair, статусы).
 *
 * ❗ Никаких `runBlocking` — все suspend или Flow.
 * Реализация через DataStore Preferences.
 */
interface SessionRepository {

    // ── Наблюдение (основной API) ───────────────────────────────────
    fun observeActivePetId(): Flow<Loadable<String?>>
    fun observeActivePairId(): Flow<Loadable<String?>>
    fun observeActivePairStatus(): Flow<String?>

    // ── Однократное чтение (suspend!) ────────────────────────────────
    suspend fun getActivePetId(): String?
    suspend fun getActivePairId(): String?
    suspend fun getActivePairStatus(): String?
    suspend fun isAccountLinked(): Boolean

    // ── Запись ───────────────────────────────────────────────────────
    suspend fun saveActivePetId(id: String)
    suspend fun saveActivePairId(id: String)
    suspend fun savePairStatus(status: String)
    suspend fun setAccountLinked(linked: Boolean)

    // ── Очистка ──────────────────────────────────────────────────────
    suspend fun clearActivePairId()
    suspend fun clearPairStatus()
    suspend fun clearActivePetId()
    suspend fun clearAllSessionData()
}