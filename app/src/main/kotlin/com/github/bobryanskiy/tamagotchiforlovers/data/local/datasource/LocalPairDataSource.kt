package com.github.bobryanskiy.tamagotchiforlovers.data.local.datasource

import com.github.bobryanskiy.tamagotchiforlovers.data.local.entity.PairEntity
import kotlinx.coroutines.flow.Flow

interface LocalPairDataSource {
    fun observePair(pairId: String): Flow<PairEntity?>
    suspend fun getPair(pairId: String): PairEntity?
    suspend fun savePair(entity: PairEntity)
    suspend fun updateStatus(pairId: String, status: String, timestamp: Long)
    suspend fun updateUserId2(pairId: String, userId2: String?, timestamp: Long)
    suspend fun deletePair(pairId: String)
}