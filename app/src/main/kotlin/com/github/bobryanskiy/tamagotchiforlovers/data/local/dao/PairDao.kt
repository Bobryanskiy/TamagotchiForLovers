package com.github.bobryanskiy.tamagotchiforlovers.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.bobryanskiy.tamagotchiforlovers.data.local.entity.PairEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PairDao {
    @Query("SELECT * FROM pairs WHERE id = :pairId LIMIT 1")
    fun observePair(pairId: String): Flow<PairEntity?>

    @Query("SELECT * FROM pairs WHERE id = :pairId LIMIT 1")
    suspend fun getPair(pairId: String): PairEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePair(pair: PairEntity)

    @Query("""
        UPDATE pairs SET 
            status = :status,
            updated_at = :timestamp
        WHERE id = :pairId
    """)
    suspend fun updateStatus(pairId: String, status: String, timestamp: Long)

    @Query("""
        UPDATE pairs SET 
            user_id_2 = :userId2,
            updated_at = :timestamp
        WHERE id = :pairId
    """)
    suspend fun updateUserId2(pairId: String, userId2: String?, timestamp: Long)

    @Query("DELETE FROM pairs WHERE id = :pairId")
    suspend fun deletePair(pairId: String)
}