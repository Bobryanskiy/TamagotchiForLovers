package com.github.bobryanskiy.tamagotchiforlovers.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.bobryanskiy.tamagotchiforlovers.data.local.entity.PetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pets WHERE id = :petId LIMIT 1")
    fun observePet(petId: String): Flow<PetEntity?>

    @Query("SELECT * FROM pets WHERE id = :petId LIMIT 1")
    suspend fun getPet(petId: String): PetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePet(pet: PetEntity)

    @Query("""
        UPDATE pets SET 
            hunger = :hunger, 
            energy = :energy, 
            cleanliness = :cleanliness, 
            happiness = :happiness, 
            updated_at = :timestamp, 
            sync_status = 'PENDING' 
        WHERE id = :petId
    """)
    suspend fun updateStats(
        petId: String,
        hunger: Int,
        energy: Int,
        cleanliness: Int,
        happiness: Int,
        timestamp: Long
    )

    @Query("""
        UPDATE pets SET 
            life_status = :status,
            death_cause = :deathCause,
            updated_at = :timestamp, 
            sync_status = 'PENDING'
        WHERE id = :petId
    """)
    suspend fun updateLifeState(
        petId: String,
        status: String,
        deathCause: String?,
        timestamp: Long
    )

    @Query("""
        UPDATE pets SET 
            current_pair_id = :pairId, 
            updated_at = :timestamp, 
            sync_status = 'PENDING' 
        WHERE id = :petId
    """)
    suspend fun updatePairId(petId: String, pairId: String?, timestamp: Long)

    @Query("""
        UPDATE pets SET 
            name = :name, 
            updated_at = :timestamp, 
            sync_status = 'PENDING' 
        WHERE id = :petId
    """)
    suspend fun updateName(petId: String, name: String, timestamp: Long)

    @Query("UPDATE pets SET sync_status = 'SYNCED' WHERE id = :petId")
    suspend fun markSynced(petId: String)

    @Query("UPDATE pets SET sync_status = 'PENDING' WHERE sync_status = 'SYNCED'")
    suspend fun markAllPending()

    @Query("UPDATE pets SET sync_status = 'PENDING' WHERE id = :petId")
    suspend fun markPending(petId: String)

    @Query("SELECT * FROM pets WHERE sync_status = 'PENDING'")
    suspend fun getPendingPets(): List<PetEntity>

    @Query("SELECT * FROM pets WHERE owner_user_id = :ownerId")
    suspend fun getPetsByOwner(ownerId: String): List<PetEntity>

    @Query("SELECT * FROM pets WHERE life_status != 'DEAD' AND life_status != 'ESCAPED'")
    suspend fun getAllActivePets(): List<PetEntity>

    @Query("""
        UPDATE pets SET 
            owner_user_id = :newOwnerId,
            updated_at = :timestamp, 
            sync_status = 'PENDING'
        WHERE owner_user_id = :oldOwnerId OR owner_user_id IS NULL
    """)
    suspend fun migrateOwnerUserId(oldOwnerId: String?, newOwnerId: String, timestamp: Long)

    @Query("DELETE FROM pets WHERE id = :petId")
    suspend fun deletePet(petId: String)
}
