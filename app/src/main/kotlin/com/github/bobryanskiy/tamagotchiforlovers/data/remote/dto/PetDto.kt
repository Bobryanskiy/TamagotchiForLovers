package com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto

import com.google.firebase.firestore.PropertyName

data class PetDto(
    @get:PropertyName("profile") @set:PropertyName("profile") var profile: ProfileDto? = null,
    @get:PropertyName("stats") @set:PropertyName("stats") var stats: StatsDto? = null,
    @get:PropertyName("life_state") @set:PropertyName("life_state") var lifeState: LifeStateDto? = null
) {
    constructor() : this(null, null, null)
}

data class ProfileDto(
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("owner_user_id") @set:PropertyName("owner_user_id") var ownerUserId: String? = "",
    @get:PropertyName("current_pair_id") @set:PropertyName("current_pair_id") var currentPairId: String? = null,
    @get:PropertyName("created_at") @set:PropertyName("created_at") var createdAt: Long = 0L,
    @get:PropertyName("abandoned_at") @set:PropertyName("abandoned_at") var abandonedAt: Long? = null
) {
    constructor() : this("", "", null, 0L, null)
}

data class StatsDto(
    @get:PropertyName("hunger") @set:PropertyName("hunger") var hunger: Int = 80,
    @get:PropertyName("energy") @set:PropertyName("energy") var energy: Int = 80,
    @get:PropertyName("cleanliness") @set:PropertyName("cleanliness") var cleanliness: Int = 80,
    @get:PropertyName("happiness") @set:PropertyName("happiness") var happiness: Int = 80,
    @get:PropertyName("updated_at") @set:PropertyName("updated_at") var updatedAt: Long = 0L
) {
    constructor() : this(80, 80, 80, 80, 0L)
}

data class LifeStateDto(
    @get:PropertyName("life_status") @set:PropertyName("life_status") var lifeStatus: String = "NORMAL",
    @get:PropertyName("death_cause") @set:PropertyName("death_cause") var deathCause: String? = null
) {
    constructor() : this("NORMAL", null)
}
