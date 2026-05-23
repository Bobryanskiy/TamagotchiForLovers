package com.github.bobryanskiy.tamagotchiforlovers.data.repository

import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.data.exception.RepositoryException
import com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper.toDomain
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.InviteKeyDto
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.PairDto
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.PendingRequestDto
import com.github.bobryanskiy.tamagotchiforlovers.di.IoDispatcher
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pair
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PendingRequest
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PairRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val petRepository: PetRepository,
    private val userRepository: UserRepository,
    private val clock: Clock,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PairRepository {

    private val requestsFlowCache = mutableMapOf<String, Flow<List<PendingRequest>>>()
    private val cacheLock = Any()

    private val scope = CoroutineScope(
        ioDispatcher + SupervisorJob() + CoroutineName("PairRepository")
    )

    override fun observePair(pairId: String): Flow<Pair?> = callbackFlow {
        Log.d("FIREBASE_DEBUG", "🔗 [REPO] Attaching listener: $pairId")

        val registration = firestore.collection("pairs").document(pairId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot?.exists() == true) {
                    val dto = snapshot.toObject<PairDto>()
                    Log.d("FIREBASE_DEBUG", "📦 [REPO] Received: invite_key=${dto?.inviteKey?.code}")
                    dto?.let { trySend(it.toDomain(snapshot.id)) }
                } else {
                    trySend(null)
                }
            }

        awaitClose {
            registration.remove()
            Log.d("FIREBASE_DEBUG", "🔌 [REPO] Removed listener: $pairId")
        }
    }

    override fun observePendingRequests(pairId: String): Flow<List<PendingRequest>> = synchronized(cacheLock) {
        requestsFlowCache.getOrPut(pairId) {
            callbackFlow {
                val registration = firestore.collection("pairs").document(pairId)
                    .addSnapshotListener { snapshot, _ ->
                        if (snapshot == null || !snapshot.exists()) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val requestData = snapshot.get("pending_request") as? Map<*, *>
                        val list = if (requestData != null && requestData["guest_id"] != null) {
                            listOf(
                                PendingRequest(
                                    guestId = requestData["guest_id"] as String,
                                    requestedAt = (requestData["requested_at"] as? Long)
                                        ?: System.currentTimeMillis()
                                )
                            )
                        } else {
                            emptyList()
                        }
                        trySend(list)
                    }
                awaitClose { registration.remove() }
            }
                .catch { e ->
                    Log.e("TAMAGOTCHI", "Error observing requests for $pairId", e)
                    emit(emptyList())
                }
                .stateIn(
                    scope = scope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    initialValue = emptyList()
                )
        }
    }

    override suspend fun createPair(
        creatorId: String,
        pairName: String,
        petId: String
    ): DomainResult<String> = try {
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
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun generateInviteKey(pairId: String): DomainResult<String> = try {
        val code = generateRandomCode()
        val expiresAt = clock.currentTimeMillis() + (5 * 60 * 1000L) // 5 минут

        firestore.collection("pairs").document(pairId)
            .update(
                "invite_key", mapOf(
                    "code" to code,
                    "expires_at" to expiresAt
                )
            )
            .await()

        DomainResult.Success(code)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun findPairByInviteKey(inviteKey: String): DomainResult<Pair> = try {
        val snapshot = firestore.collection("pairs")
            .whereEqualTo("invite_key.code", inviteKey.uppercase())
            .limit(1)
            .get()
            .await()

        if (snapshot.isEmpty) return DomainResult.Failure(PairError.PairNotFound)

        val doc = snapshot.documents.first()
        val dto = doc.toObject<PairDto>() ?: return DomainResult.Failure(PairError.PairNotFound)
        val pair = dto.toDomain(doc.id)

        // Проверка срока действия ключа
        val inviteKeyMap = doc.get("invite_key") as? Map<*, *>
        val expiresAt = inviteKeyMap?.get("expires_at") as? Long
        if (expiresAt != null && clock.currentTimeMillis() > expiresAt) {
            return DomainResult.Failure(PairError.InvalidRequest)
        }

        // Проверка, что пара ещё может принимать участников
        if (pair.userId2 != null) {
            return DomainResult.Failure(PairError.AlreadyJoined)
        }

        DomainResult.Success(pair)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun requestJoin(pairId: String, guestId: String): DomainResult<Unit> = try {
        firestore.runTransaction { transaction ->
            val ref = firestore.collection("pairs").document(pairId)
            val snapshot = transaction.get(ref)

            if (!snapshot.exists()) throw IllegalArgumentException("Пара не найдена")

            val userId1 = snapshot.getString("user_id_1")
            val userId2 = snapshot.getString("user_id_2")
            val status = snapshot.getString("status")

            if (userId2 != null) throw IllegalStateException("Пара уже заполнена")
            if (status != PairStatus.PENDING.name) throw IllegalStateException("Пара не активна")
            if (guestId == userId1) throw IllegalStateException("Вы уже являетесь создателем пары")

            transaction.update(
                ref,
                "pending_request", mapOf(
                    "guest_id" to guestId,
                    "requested_at" to FieldValue.serverTimestamp()
                )
            )
        }.await()

        userRepository.updateUserSession(guestId, null, pairId)
        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        val error = when (e) {
            is IllegalStateException -> PairError.AlreadyJoined
            is IllegalArgumentException -> PairError.PairNotFound
            else -> mapToPairError(e)
        }
        DomainResult.Failure(error)
    }

    override suspend fun acceptJoinRequest(
        pairId: String,
        guestId: String,
        callerId: String
    ): DomainResult<Unit> = try {
        // Проверка прав: только хост может принимать
        val pairDoc = firestore.collection("pairs").document(pairId).get().await()
        val hostId = pairDoc.getString("user_id_1")

        if (callerId != hostId) {
            return DomainResult.Failure(PairError.CreatorOnly)
        }

        val petId = pairDoc.getString("current_pet_id")
            ?: return DomainResult.Failure(PairError.PairNotFound)

        // Обновляем пару
        firestore.collection("pairs").document(pairId).update(
            mapOf(
                "user_id_2" to guestId,
                "status" to PairStatus.ACTIVE.name,
                "invite_key" to null,
                "pending_request" to FieldValue.delete(),
                "updated_at" to FieldValue.serverTimestamp()
            )
        ).await()

        // Обновляем сессии пользователей
        userRepository.updateUserSession(guestId, petId, pairId)
        userRepository.updateUserSession(hostId, petId, pairId)

        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun rejectJoinRequest(
        pairId: String,
        guestId: String,
        callerId: String
    ): DomainResult<Unit> = try {
        val pairDoc = firestore.collection("pairs").document(pairId).get().await()
        val hostId = pairDoc.getString("user_id_1")

        if (callerId != hostId) {
            return DomainResult.Failure(PairError.CreatorOnly)
        }

        // Просто удаляем запрос
        firestore.collection("pairs").document(pairId)
            .update("pending_request", FieldValue.delete())
            .await()

        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun leaveSession(pairId: String, userId: String): DomainResult<Unit> = try {
        firestore.collection("pairs").document(pairId).update(
            mapOf(
                "user_id_2" to FieldValue.delete(),
                "status" to PairStatus.PENDING.name,
                "invite_key" to null,
                "updated_at" to FieldValue.serverTimestamp()
            )
        ).await()

        userRepository.updateUserSession(userId, null, null)
        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun endSession(pairId: String, callerId: String): DomainResult<Unit> = try {
        val pairDoc = firestore.collection("pairs").document(pairId).get().await()
        val hostId = pairDoc.getString("user_id_1")
        val userId2 = pairDoc.getString("user_id_2")

        if (callerId != hostId) {
            return DomainResult.Failure(PairError.CreatorOnly)
        }

        firestore.collection("pairs").document(pairId).update(
            mapOf(
                "status" to PairStatus.ENDED.name,
                "ended_at" to FieldValue.serverTimestamp(),
                "user_id_2" to FieldValue.delete(),
                "updated_at" to FieldValue.serverTimestamp()
            )
        ).await()

        hostId.let { userRepository.updateUserSession(it, null, null) }
        userId2?.let { userRepository.updateUserSession(it, null, null) }

        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun updatePairName(pairId: String, newName: String): DomainResult<Unit> = try {
        firestore.collection("pairs").document(pairId)
            .update("name", newName)
            .await()
        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun kickPartner(pairId: String, callerId: String): DomainResult<Unit> = try {
        val doc = firestore.collection("pairs").document(pairId).get().await()
        val hostId = doc.getString("user_id_1")

        if (callerId != hostId) {
            return DomainResult.Failure(PairError.CreatorOnly)
        }

        firestore.collection("pairs").document(pairId).update(
            mapOf(
                "user_id_2" to FieldValue.delete(),
                "status" to PairStatus.PENDING.name,
                "invite_key" to null,
                "pending_request" to FieldValue.delete(),
                "updated_at" to FieldValue.serverTimestamp()
            )
        ).await()

        // Сбросим сессию у выгнанного игрока
        val guestId = doc.getString("user_id_2")
        guestId?.let {
            userRepository.updateUserSession(it, null, null)
        }

        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun getPair(pairId: String): Pair? {
        val snapshot = firestore.collection("pairs").document(pairId).get().await()
        return if (snapshot.exists()) {
            snapshot.toObject<PairDto>()?.toDomain(pairId)
        } else null
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
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> PairError.Network
            else -> PairError.Unknown
        }
        is CancellationException -> throw error
        is IllegalArgumentException, is IllegalStateException -> PairError.InvalidRequest
        else -> PairError.Unknown
    }
}