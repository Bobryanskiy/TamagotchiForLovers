package com.github.bobryanskiy.tamagotchiforlovers.data.repository

import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.data.local.datasource.LocalPairDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper.toDomain
import com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper.toEntity
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.PairDto
import com.github.bobryanskiy.tamagotchiforlovers.di.IoDispatcher
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetPair
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PendingRequest
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.PairResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PairRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val local: LocalPairDataSource,
    private val petRepository: PetRepository,
    private val userRepository: UserRepository,
    private val clock: Clock,
    private val logger: AppLogger,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PairRepository {

    private val requestsFlowCache = mutableMapOf<String, SharedFlow<List<PendingRequest>>>()
    private val cacheLock = Any()

    private val scope = CoroutineScope(
        ioDispatcher + SupervisorJob() + CoroutineName("PairRepository")
    )

    companion object {
        private const val TAG = "PairRepository"
        private const val INITIAL_RETRY_DELAY_MS = 1_000L
        private const val MAX_RETRY_DELAY_MS = 30_000L
        private const val MAX_RETRY_ATTEMPTS = 10L
        private const val INVITE_KEY_TTL_MS = 5 * 60 * 1_000L
    }

    override fun observePair(pairId: String): Flow<PetPair?> = callbackFlow {
        logger.d(TAG, "🔗 Attaching listener: $pairId")

        local.getPair(pairId)?.let { entity ->
            trySend(entity.toDomain())
        }

        var registration: ListenerRegistration? = null

        registration = firestore.collection("pairs").document(pairId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logger.w(TAG, "⚠️ Snapshot error for $pairId, will retry", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot?.exists() == true) {
                    val dto = snapshot.toObject<PairDto>()
                    dto?.let {
                        val pair = it.toDomain(snapshot.id)
                        scope.launch {
                            runCatching { local.savePair(it.toEntity(snapshot.id)) }
                        }
                        trySend(pair)
                    }
                } else {
                    trySend(null)
                }
            }

        awaitClose {
            registration.remove()
            logger.d(TAG, "🔌 Removed listener: $pairId")
        }
    }.retryWhen { cause, attempt ->
        if (attempt >= MAX_RETRY_ATTEMPTS) {
            logger.e(TAG, "❌ Max retries reached for $pairId", cause)
            return@retryWhen false
        }
        val delayMs = (INITIAL_RETRY_DELAY_MS * (attempt + 1)).coerceAtMost(MAX_RETRY_DELAY_MS)
        logger.w(TAG, "🔄 Retrying (attempt ${attempt + 1}) in ${delayMs}ms")
        delay(delayMs)
        true
    }

    override fun observePendingRequests(pairId: String): Flow<List<PendingRequest>> = synchronized(cacheLock) {
        requestsFlowCache.getOrPut(pairId) {
            callbackFlow {
                val registration = firestore.collection("pairs").document(pairId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            logger.w(TAG, "⚠️ observePendingRequests error", error)
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        if (snapshot == null || !snapshot.exists()) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val requestData = snapshot.get("pending_request") as? Map<*, *>
                        val list = if (requestData?.get("guest_id") is String) {
                            listOf(
                                PendingRequest(
                                    guestId = requestData["guest_id"] as String,
                                    requestedAt = (requestData["requested_at"] as? Timestamp)
                                        ?.toDate()?.time ?: System.currentTimeMillis()
                                )
                            )
                        } else emptyList()
                        trySend(list)
                    }
                awaitClose {
                    registration.remove()
                }
            }
                .catch { e ->
                    logger.e(TAG, "Error observing requests for $pairId", e)
                    emit(emptyList())
                }
                .stateIn(
                    scope = scope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
                    initialValue = emptyList()
                )
        }
    }

    fun clearCacheForPair(pairId: String) = synchronized(cacheLock) {
        requestsFlowCache.remove(pairId)
    }

    override suspend fun createPair(
        creatorId: String,
        pairName: String,
        petId: String
    ): PairResult<String> = try {
        val pairId = firestore.collection("pairs").document().id
        val now = clock.currentTimeMillis()

        val data = mapOf(
            "name" to pairName,
            "user_id_1" to creatorId,
            "user_id_2" to null,
            "current_pet_id" to petId,
            "status" to PairStatus.PENDING.name,
            "created_at" to now,
            "updated_at" to now,
            "ended_at" to null,
            "invite_key" to null,
            "pending_request" to null
        )

        firestore.collection("pairs").document(pairId).set(data).await()
        petRepository.updatePairId(petId, pairId)
        userRepository.updateUserSession(creatorId, petId, pairId)
        DomainResult.Success(pairId)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e)).let {
            DomainResult.Failure(mapToPairError(e))
        }
    }

    override suspend fun generateInviteKey(pairId: String): PairResult<String> = try {
        val code = generateRandomCode()
        val expiresAt = clock.currentTimeMillis() + INVITE_KEY_TTL_MS
        firestore.collection("pairs").document(pairId)
            .update("invite_key", mapOf("code" to code, "expires_at" to expiresAt))
            .await()
        DomainResult.Success(code)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun findPairByInviteKey(inviteKey: String): PairResult<PetPair> = try {
        val snapshot = firestore.collection("pairs")
            .whereEqualTo("invite_key.code", inviteKey.uppercase())
            .limit(1)
            .get()
            .await()

        if (snapshot.isEmpty) return DomainResult.Failure(PairError.PairNotFound)

        val doc = snapshot.documents.first()
        val dto = doc.toObject<PairDto>() ?: return DomainResult.Failure(PairError.PairNotFound)
        val pair = dto.toDomain(doc.id)

        val expiresAt = (doc.get("invite_key") as? Map<*, *>)?.get("expires_at") as? Long
        if (expiresAt != null && clock.currentTimeMillis() > expiresAt) {
            return DomainResult.Failure(PairError.InvalidRequest)
        }
        if (pair.userId2 != null) return DomainResult.Failure(PairError.AlreadyJoined)
        if (pair.status != PairStatus.PENDING) return DomainResult.Failure(PairError.InvalidRequest)

        DomainResult.Success(pair)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun requestJoin(pairId: String, guestId: String): PairResult<Unit> = try {
        val pairDoc = firestore.collection("pairs").document(pairId).get().await()

        if (!pairDoc.exists()) return DomainResult.Failure(PairError.PairNotFound)

        val userId1 = pairDoc.getString("user_id_1")
        val userId2 = pairDoc.getString("user_id_2")
        val status = pairDoc.getString("status")

        if (userId2 != null) return DomainResult.Failure(PairError.AlreadyJoined)
        if (status != PairStatus.PENDING.name) return DomainResult.Failure(PairError.InvalidRequest)
        if (guestId == userId1) return DomainResult.Failure(PairError.InvalidRequest)

        firestore.collection("pairs").document(pairId)
            .update(
                "pending_request", mapOf(
                    "guest_id" to guestId,
                    "requested_at" to FieldValue.serverTimestamp()
                )
            )
            .await()

        userRepository.updateUserSession(guestId, null, pairId)
        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        logger.e(TAG, "Request join failed", e)
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun acceptJoinRequest(
        pairId: String,
        guestId: String,
        callerId: String
    ): PairResult<Unit> = try {
        val pairDoc = firestore.collection("pairs").document(pairId).get().await()
        val hostId = pairDoc.getString("user_id_1")
        if (callerId != hostId) return DomainResult.Failure(PairError.CreatorOnly)

        firestore.collection("pairs").document(pairId).update(
            mapOf(
                "user_id_2" to guestId,
                "status" to PairStatus.ACTIVE.name,
                "invite_key" to null,
                "pending_request" to FieldValue.delete(),
                "updated_at" to System.currentTimeMillis()
            )
        ).await()

        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun rejectJoinRequest(
        pairId: String,
        guestId: String,
        callerId: String
    ): PairResult<Unit> = try {
        val pairDoc = firestore.collection("pairs").document(pairId).get().await()
        val hostId = pairDoc.getString("user_id_1")
        if (callerId != hostId) return DomainResult.Failure(PairError.CreatorOnly)

        firestore.collection("pairs").document(pairId)
            .update("pending_request", FieldValue.delete())
            .await()
        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun leaveSession(pairId: String, userId: String): PairResult<Unit> = try {
        firestore.collection("pairs").document(pairId).update(
            mapOf(
                "user_id_2" to FieldValue.delete(),
                "status" to PairStatus.PENDING.name,
                "invite_key" to null,
                "pending_request" to FieldValue.delete(),
                "updated_at" to System.currentTimeMillis()
            )
        ).await()

        userRepository.updateUserSession(userId, null, null)
        clearCacheForPair(pairId)
        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun endSession(pairId: String, callerId: String): PairResult<Unit> = try {
        val pairDoc = firestore.collection("pairs").document(pairId).get().await()
        val hostId = pairDoc.getString("user_id_1")
        val userId2 = pairDoc.getString("user_id_2")
        if (callerId != hostId) return DomainResult.Failure(PairError.CreatorOnly)

        firestore.collection("pairs").document(pairId).update(
            mapOf(
                "status" to PairStatus.ENDED.name,
                "ended_at" to System.currentTimeMillis(),
                "user_id_2" to FieldValue.delete(),
                "pending_request" to FieldValue.delete(),
                "invite_key" to null,
                "updated_at" to System.currentTimeMillis()
            )
        ).await()

        hostId.let { userRepository.updateUserSession(it, null, null) }
        userId2?.let { userRepository.updateUserSession(it, null, null) }
        clearCacheForPair(pairId)

        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun updatePairName(pairId: String, newName: String): PairResult<Unit> = try {
        firestore.collection("pairs").document(pairId).update("name", newName).await()
        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun kickPartner(pairId: String, callerId: String): PairResult<Unit> = try {
        val doc = firestore.collection("pairs").document(pairId).get().await()
        val hostId = doc.getString("user_id_1")
        if (callerId != hostId) return DomainResult.Failure(PairError.CreatorOnly)

        firestore.collection("pairs").document(pairId).update(
            mapOf(
                "user_id_2" to FieldValue.delete(),
                "status" to PairStatus.PENDING.name,
                "invite_key" to null,
                "pending_request" to FieldValue.delete(),
                "updated_at" to System.currentTimeMillis()
            )
        ).await()

        doc.getString("user_id_2")?.let { userRepository.updateUserSession(it, null, null) }
        clearCacheForPair(pairId)
        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun getPair(pairId: String): PairResult<PetPair?> = try {
        val snapshot = firestore.collection("pairs").document(pairId).get().await()
        val pair = if (snapshot.exists()) snapshot.toObject<PairDto>()?.toDomain(pairId) else null
        DomainResult.Success(pair)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    private fun generateRandomCode(length: Int = 6): String {
        val chars = ('A'..'Z') + ('0'..'9')
        return (1..length).map { chars.random() }.joinToString("")
    }

    private fun mapToPairError(error: Throwable): PairError = when (error) {
        is FirebaseFirestoreException -> when (error.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            FirebaseFirestoreException.Code.NOT_FOUND -> PairError.InvalidRequest
            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
            FirebaseFirestoreException.Code.CANCELLED -> PairError.Network
            else -> PairError.Unknown
        }
        is CancellationException -> throw error
        is IllegalArgumentException, is IllegalStateException -> PairError.InvalidRequest
        else -> PairError.Unknown
    }
}