package com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class PetDto(
    @get:PropertyName("profile") val profile: ProfileDto? = null,
    @get:PropertyName("stats") val stats: StatsDto? = null,
    @get:PropertyName("life_state") val lifeState: LifeStateDto? = null,
    @get:PropertyName("sync_status") val syncStatus: String = "SYNCED"
)

data class ProfileDto(
    @PropertyName("name") val name: String = "",
    @PropertyName("owner_user_id") val ownerUserId: String? = "",
    @PropertyName("current_pair_id") val currentPairId: String? = null,
    @PropertyName("created_at") val createdAt: Long = 0L,
    @PropertyName("abandoned_at") val abandonedAt: Long? = null
)

data class StatsDto(
    @PropertyName("hunger") val hunger: Int = 80,
    @PropertyName("energy") val energy: Int = 80,
    @PropertyName("cleanliness") val cleanliness: Int = 80,
    @PropertyName("happiness") val happiness: Int = 80,
    @PropertyName("updated_at") val updatedAt: Long = 0L
)

data class LifeStateDto(
    @PropertyName("life_status") val lifeStatus: String = "NORMAL",
    @PropertyName("actions_blocked") val isActionsBlocked: Boolean = false,
    @PropertyName("decay_multiplier") val decayMultiplier: Float = 1.0f,
    @PropertyName("recovery_end_time") val recoveryEndTime: Long? = null
)