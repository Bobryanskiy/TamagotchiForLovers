package com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto

/**
 * Константы полей Firestore.
 *
 * Как использовать:
 * 1. В DTO (@PropertyName): Используй только ИМЯ ПОЛЯ (без точек).
 *    Пример: @PropertyName(PetFields.PROFILE_NAME)
 *
 * 2. В Repository (update/mapOf): Используй ПОЛНЫЙ ПУТЬ (через точку).
 *    Пример: mapOf(PetKeys.PROFILE_NAME to "Barsik")
 *    или: mapOf(PetKeys.INVITE_KEY_CODE to "123")
 */

//  PET KEYS
object PetKeys {
    // --- Profile ---
    const val PROFILE = "profile"
    const val PROFILE_NAME = "${PROFILE}.name"
    const val PROFILE_OWNER_USER_ID = "${PROFILE}.owner_user_id"
    const val PROFILE_CURRENT_PAIR_ID = "${PROFILE}.current_pair_id"
    const val PROFILE_CREATED_AT = "${PROFILE}.created_at"
    const val PROFILE_ABANDONED_AT = "${PROFILE}.abandoned_at"

    // --- Stats ---
    const val STATS = "stats"
    const val STATS_HUNGER = "${STATS}.hunger"
    const val STATS_ENERGY = "${STATS}.energy"
    const val STATS_CLEANLINESS = "${STATS}.cleanliness"
    const val STATS_HAPPINESS = "${STATS}.happiness"
    const val STATS_UPDATED_AT = "${STATS}.updated_at"

    // --- Life State ---
    const val LIFE_STATE = "life_state"
    const val LIFE_STATUS = "${LIFE_STATE}.life_status"
    const val LIFE_DEATH_STATUS = "${LIFE_STATE}.death_cause"

    // --- System / Sync ---
    const val SYNC_STATUS = "sync_status"
}

//  PAIR KEYS
object PairKeys {
    const val NAME = "name"
    const val UPDATED_AT = "updated_at"
    const val USER_ID_1 = "user_id_1"
    const val USER_ID_2 = "user_id_2"
    const val CURRENT_PET_ID = "current_pet_id"
    const val STATUS = "status"
    const val CREATED_AT = "created_at"
    const val ENDED_AT = "ended_at"

    // --- Nested: Invite Key ---
    const val INVITE_KEY = "invite_key"
    const val INVITE_KEY_CODE = "${INVITE_KEY}.code"
    const val INVITE_KEY_EXPIRES_AT = "${INVITE_KEY}.expires_at"

    // --- Nested: Pending Request ---
    const val PENDING_REQUEST = "pending_request"
    const val PENDING_REQUEST_GUEST_ID = "${PENDING_REQUEST}.guest_id"
    const val PENDING_REQUEST_REQUESTED_AT = "${PENDING_REQUEST}.requested_at"
}
