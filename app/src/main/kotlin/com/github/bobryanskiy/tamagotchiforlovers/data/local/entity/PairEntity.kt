package com.github.bobryanskiy.tamagotchiforlovers.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pairs",
    indices = [
        Index("user_id_1"),           // для поиска по creator
        Index("user_id_2"),           // для поиска по guest
        Index("status"),              // для фильтрации
        Index("invite_code")          // для поиска по коду
    ]
)
data class PairEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,

    @ColumnInfo(name = "user_id_1") val userId1: String,
    @ColumnInfo(name = "user_id_2") val userId2: String?,

    @ColumnInfo(name = "current_pet_id") val currentPetId: String,

    @ColumnInfo(name = "status") val status: String,

    @ColumnInfo(name = "invite_code") val inviteCode: String?,
    @ColumnInfo(name = "invite_expires_at") val inviteExpiresAt: Long?,

    @ColumnInfo(name = "request_guest_id") val requestGuestId: String?,
    @ColumnInfo(name = "request_requested_at") val requestRequestedAt: Long?,

    @ColumnInfo(name = "created_at") val createdAt: Long?,
    @ColumnInfo(name = "updated_at") val updatedAt: Long?,
    @ColumnInfo(name = "ended_at") val endedAt: Long?,
    @ColumnInfo(name = "sync_status") val syncStatus: String = "SYNCED"
)
