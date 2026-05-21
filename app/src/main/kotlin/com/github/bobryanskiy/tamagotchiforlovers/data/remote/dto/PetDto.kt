package com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class PetDto(
    @PropertyName("profile") val profile: ProfileDto? = null,
    @PropertyName("stats") val stats: StatsDto? = null,
    @PropertyName("life_state") val lifeState: LifeStateDto? = null,
    @PropertyName("sync_status") val syncStatus: String = "SYNCED"
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
    @PropertyName("updated_at") @ServerTimestamp val updatedAt: Date? = null
)

data class LifeStateDto(
    @PropertyName("life_status") val lifeStatus: String = "NORMAL",
    @PropertyName("is_actions_blocked") val isActionsBlocked: Boolean = false,
    @PropertyName("decay_multiplier") val decayMultiplier: Float = 1.0f,
    @PropertyName("recovery_end_time") val recoveryEndTime: Long? = null
)