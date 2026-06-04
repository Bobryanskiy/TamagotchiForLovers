package com.github.bobryanskiy.tamagotchiforlovers.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pets",
    indices = [
        Index("owner_user_id"),      // для getPetsByOwner
        Index("current_pair_id"),    // для поиска по паре
        Index("sync_status"),        // для getPendingPets
        Index("updated_at")          // для сортировки
    ]
)
data class PetEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "owner_user_id") val ownerUserId: String?,
    @ColumnInfo(name = "current_pair_id") val currentPairId: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "abandoned_at") val abandonedAt: Long?,

    @ColumnInfo(name = "life_status") val lifeStatus: String,
    @ColumnInfo(name = "death_cause") val deathCause: String?,

    @ColumnInfo(name = "hunger") val hunger: Int,
    @ColumnInfo(name = "energy") val energy: Int,
    @ColumnInfo(name = "cleanliness") val cleanliness: Int,
    @ColumnInfo(name = "happiness") val happiness: Int,

    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "sync_status") val syncStatus: String = "SYNCED"
) {
    companion object {
        fun createNew(
            petId: String,
            name: String,
            ownerUserId: String?,
            syncStatus: String = "PENDING"
        ): PetEntity = PetEntity(
            id = petId,
            name = name,
            ownerUserId = ownerUserId,
            currentPairId = null,
            createdAt = System.currentTimeMillis(),
            lifeStatus = "NORMAL",
            deathCause = null,
            abandonedAt = null,
            hunger = 80,
            energy = 80,
            cleanliness = 80,
            happiness = 80,
            updatedAt = System.currentTimeMillis(),
            syncStatus = syncStatus
        )
    }
}
