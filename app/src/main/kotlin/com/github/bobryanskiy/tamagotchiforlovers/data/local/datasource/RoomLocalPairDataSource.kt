package com.github.bobryanskiy.tamagotchiforlovers.data.local.datasource

import com.github.bobryanskiy.tamagotchiforlovers.data.local.dao.PairDao
import com.github.bobryanskiy.tamagotchiforlovers.data.local.entity.PairEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomLocalPairDataSource @Inject constructor(
    private val pairDao: PairDao
) : LocalPairDataSource {
    override fun observePair(pairId: String): Flow<PairEntity?> = pairDao.observePair(pairId)
    override suspend fun getPair(pairId: String): PairEntity? = pairDao.getPair(pairId)
    override suspend fun savePair(entity: PairEntity) = pairDao.savePair(entity)
    override suspend fun updateStatus(pairId: String, status: String, timestamp: Long) =
        pairDao.updateStatus(pairId, status, timestamp)
    override suspend fun updateUserId2(pairId: String, userId2: String?, timestamp: Long) =
        pairDao.updateUserId2(pairId, userId2, timestamp)
    override suspend fun deletePair(pairId: String) = pairDao.deletePair(pairId)
    override suspend fun updateInviteKey(pairId: String, code: String?, expiresAt: Long?, timestamp: Long) =
        pairDao.updateInviteKey(pairId, code, expiresAt, timestamp)
    override suspend fun updatePendingRequest(pairId: String, guestId: String?, requestedAt: Long?) =
        pairDao.updatePendingRequest(pairId, guestId, requestedAt)
}
