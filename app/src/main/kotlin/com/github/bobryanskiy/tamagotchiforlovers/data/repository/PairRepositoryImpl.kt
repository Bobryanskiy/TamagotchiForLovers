package com.github.bobryanskiy.tamagotchiforlovers.data.repository

import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.data.local.datasource.LocalPairDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper.toDomain
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.datasource.FirestoreRemoteDataSource
import com.github.bobryanskiy.tamagotchiforlovers.di.IoDispatcher
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PendingRequest
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetPair
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PairResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.NameLimits
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PairRepositoryImpl @Inject constructor(
    private val remote: FirestoreRemoteDataSource,
    private val local: LocalPairDataSource,
    private val petRepository: PetRepository,
    private val userRepository: UserRepository,
    private val clock: Clock,
    private val logger: AppLogger,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PairRepository {

    companion object {
        private const val TAG = "PairRepo"
        private const val INVITE_KEY_TTL_MS = 5 * 60 * 1_000L
    }

    override fun observePair(pairId: String): Flow<PetPair?> =
        remote.observePair(pairId)
            .map { dto -> dto?.toDomain(pairId) }
            .catch { e ->
                logger.e(TAG, "Error observing pair $pairId", e)
                emit(null)
            }

    override fun observePendingRequests(pairId: String): Flow<List<PendingRequest>> =
        remote.observePendingRequests(pairId)
            .map { list ->
                list.mapNotNull { map ->
                    val guestId = map["guest_id"] as? String ?: return@mapNotNull null
                    val requestedAt = (map["requested_at"] as? com.google.firebase.Timestamp)
                        ?.toDate()?.time ?: clock.currentTimeMillis()
                    PendingRequest(guestId = guestId, requestedAt = requestedAt)
                }
            }
            .catch { e ->
                logger.e(TAG, "Error observing requests for $pairId", e)
                emit(emptyList())
            }

    override suspend fun createPair(
        creatorId: String,
        pairName: String,
        petId: String
    ): PairResult<String> = try {
        val pairId = remote.generateDocumentId("pairs")
        val now = clock.currentTimeMillis()

        val dto = com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.PairDto(
            name = pairName,
            userId1 = creatorId,
            userId2 = null,
            currentPetId = petId,
            status = PairStatus.PENDING.name,
            createdAt = now,
            updatedAt = now,
            inviteKey = null,
            pendingRequest = null
        )

        remote.upsertPair(pairId, dto)

        petRepository.updatePairId(petId, pairId)
        userRepository.updateUserSession(creatorId, petId, pairId)

        DomainResult.Success(pairId)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        logger.e(TAG, "createPair failed", e)
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun generateInviteKey(pairId: String): PairResult<String> = try {
        val code = generateRandomCode()
        val expiresAt = clock.currentTimeMillis() + INVITE_KEY_TTL_MS

        remote.generateInviteKey(pairId, code, expiresAt)

        DomainResult.Success(code)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        logger.e(TAG, "generateInviteKey failed", e)
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun findPairByInviteKey(inviteKey: String): PairResult<PetPair> = try {
        val (pairId, dto) = remote.findPairByInviteKey(inviteKey)
            ?: return DomainResult.Failure(PairError.PairNotFound)

        val pair = dto.toDomain(pairId)

        if (pair.userId2 != null) return DomainResult.Failure(PairError.AlreadyJoined)
        if (pair.status != PairStatus.PENDING) return DomainResult.Failure(PairError.InvalidRequest)

        DomainResult.Success(pair)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        logger.e(TAG, "findPairByInviteKey failed", e)
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun requestJoin(pairId: String, guestId: String): PairResult<Unit> = try {
        remote.requestJoin(pairId, guestId)

        userRepository.updateUserSession(guestId, null, pairId)

        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        logger.e(TAG, "requestJoin failed", e)
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun acceptJoinRequest(
        pairId: String,
        guestId: String,
        callerId: String
    ): PairResult<Unit> = try {
        remote.acceptJoinRequest(pairId, guestId)
        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        logger.e(TAG, "acceptJoinRequest failed", e)
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun rejectJoinRequest(
        pairId: String,
        guestId: String,
        callerId: String
    ): PairResult<Unit> = try {
        remote.rejectJoinRequest(pairId, guestId)
        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        logger.e(TAG, "rejectJoinRequest failed", e)
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun leaveSession(pairId: String, userId: String): PairResult<Unit> = try {
        remote.leaveSession(pairId, userId)

        userRepository.updateUserSession(userId, null, null)

        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        logger.e(TAG, "leaveSession failed", e)
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun endSession(pairId: String, callerId: String): PairResult<Unit> = try {
        remote.endSession(pairId, callerId)

        userRepository.updateUserSession(callerId, null, null)

        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        logger.e(TAG, "endSession failed", e)
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun kickPartner(pairId: String, callerId: String): PairResult<Unit> = try {
        val code = generateRandomCode()
        val expiresAt = clock.currentTimeMillis() + INVITE_KEY_TTL_MS

        remote.kickPartner(pairId, callerId, code, expiresAt)
        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        logger.e(TAG, "kickPartner failed", e)
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun updatePairName(pairId: String, newName: String): PairResult<Unit> = try {
        val trimmed = newName.trim()
        if (trimmed.length !in NameLimits.PAIR_NAME_MIN..NameLimits.PAIR_NAME_MAX) {
            return DomainResult.Failure(PairError.InvalidInput)
        }

        remote.updatePairName(pairId, trimmed)

        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        logger.e(TAG, "updatePairName failed", e)
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun getPair(pairId: String): PairResult<PetPair?> = try {
        val dto = remote.getPair(pairId)
        val pair = dto?.toDomain(pairId)
        DomainResult.Success(pair)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        logger.e(TAG, "getPair failed", e)
        DomainResult.Failure(mapToPairError(e))
    }

    private fun generateRandomCode(length: Int = 6): String {
        val chars = ('A'..'Z') + ('0'..'9')
        return (1..length).map { chars.random() }.joinToString("")
    }

    private fun mapToPairError(error: Throwable): PairError = when (error) {
        is com.google.firebase.firestore.FirebaseFirestoreException -> when (error.code) {
            com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND -> PairError.PairNotFound
            com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                if (userRepository.getCurrentUserId() == null) PairError.NotAuthenticated
                else PairError.InvalidRequest
            }
            com.google.firebase.firestore.FirebaseFirestoreException.Code.UNAVAILABLE,
            com.google.firebase.firestore.FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
            com.google.firebase.firestore.FirebaseFirestoreException.Code.CANCELLED -> PairError.Network
            else -> PairError.Unknown
        }
        is CancellationException -> throw error
        is IllegalArgumentException, is IllegalStateException -> PairError.InvalidRequest
        else -> PairError.Unknown
    }
}
