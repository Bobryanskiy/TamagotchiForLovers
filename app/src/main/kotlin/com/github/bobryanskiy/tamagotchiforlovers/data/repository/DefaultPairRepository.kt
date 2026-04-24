package com.github.bobryanskiy.tamagotchiforlovers.data.repository

import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.data.exception.RepositoryException
import com.github.bobryanskiy.tamagotchiforlovers.data.model.dto.PairDto
import com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper.toDomain
import com.github.bobryanskiy.tamagotchiforlovers.di.IoDispatcher
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pair
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultPairRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val petRepository: PetRepository,
    private val userRepository: UserRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PairRepository {
    private val pairFlowCache = mutableMapOf<String, Flow<Pair?>>()
    private val cacheLock = Any()
    private val scope = CoroutineScope(ioDispatcher + SupervisorJob() + CoroutineName("PairRepo"))

    override suspend fun getCurrentUserId(): String? = userRepository.getCurrentUserId()

    override fun observePair(pairId: String): Flow<Pair?> = synchronized(cacheLock) {
        pairFlowCache.getOrPut(pairId) {
            callbackFlow {
                val registration = firestore.collection("pairs").document(pairId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(RepositoryException(mapToPairError(error)))
                            return@addSnapshotListener
                        }

                        if (snapshot == null || !snapshot.exists()) {
                            trySend(null)
                            return@addSnapshotListener
                        }
                        val dto = snapshot.toObject<PairDto>()
                        val result = trySend(dto?.toDomain(pairId))
                        if (result.isFailure) Log.w("TAMAGOTCHI", "Failed to emit pair $pairId")
                    }
                awaitClose {
                    registration.remove()
                    Log.d("TAMAGOTCHI", "Listener removed for pair: $pairId")
                }
            }.stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = null
            )
        }
    }

    override suspend fun createPair(creatorId: String, pairName: String, petId: String): DomainResult<String> = try {
        val pairId = firestore.collection("pairs").document().id
        val now = System.currentTimeMillis()
        val data = mapOf(
            "name" to pairName,
            "userId1" to creatorId,
            "userId2" to null,
            "currentPetId" to petId,
            "status" to PairStatus.PENDING.name,
            "createdAt" to now,
            "endedAt" to null
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
        val code = (1..6).map {('A'..'Z').random() }.joinToString("")
        val expiresAt = System.currentTimeMillis() + (5 * 60 * 1000)
        firestore.collection("pairs").document(pairId)
            .update("inviteKey", mapOf("code" to code, "expiresAt" to expiresAt))
            .await()
        DomainResult.Success(code)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun findPairByInviteKey(inviteKey: String): DomainResult<Pair> = try {
        val snapshot = firestore.collection("pairs")
            .whereEqualTo("inviteKey.code", inviteKey.uppercase())
            .limit(1)
            .get()
            .await()
        
        if (snapshot.isEmpty) {
            return DomainResult.Failure(PairError.NotFound)
        }
        
        val doc = snapshot.documents.first()
        val dto = doc.toObject<PairDto>() ?: return DomainResult.Failure(PairError.NotFound)
        val pair = dto.toDomain(doc.id)
        
        // Проверяем, не истёк ли срок действия кода
        val inviteKeyMap = doc.get("inviteKey") as? Map<*, *>
        val expiresAt = inviteKeyMap?.get("expiresAt") as? Long
        if (expiresAt != null && System.currentTimeMillis() > expiresAt) {
            return DomainResult.Failure(PairError.InvalidRequest)
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
            if (snapshot.getString("userId2") != null) throw IllegalStateException("Пара уже заполнена")
            
            // Проверяем, не является ли гость уже участником пары
            val userId1 = snapshot.getString("userId1")
            if (guestId == userId1) throw IllegalStateException("Вы уже являетесь создателем этой пары")
            
            transaction.update(ref, "pendingRequest", mapOf("guestId" to guestId, "requestedAt" to System.currentTimeMillis()))
        }.await()
        userRepository.updateUserSession(guestId, null, pairId)
        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        val error = when (e) {
            is IllegalStateException -> PairError.AlreadyJoined
            else -> mapToPairError(e)
        }
        DomainResult.Failure(error)
    }

    override suspend fun acceptPlayer(pairId: String, guestId: String): DomainResult<Unit> = try {
        val pairDoc = firestore.collection("pairs").document(pairId).get().await()
        val petId = pairDoc.getString("currentPetId") ?: return DomainResult.Failure(PairError.NotFound)
        val guestUserDoc = firestore.collection("users").document(guestId).get().await()
        firestore.collection("pairs").document(pairId).update(mapOf(
            "userId2" to guestId,
            "currentPetId" to petId,
            "status" to PairStatus.ACTIVE.name,
            "inviteKey" to null,
            "pendingRequest" to null,
            "updatedAt" to System.currentTimeMillis()
        )).await()
        userRepository.updateUserSession(guestId, petId, pairId)
        val hostId = pairDoc.getString("userId1")
        if (hostId != null) {
            userRepository.updateUserSession(hostId, petId, pairId)
        }
        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun leaveSession(pairId: String, guestId: String): DomainResult<Unit> = try {
        firestore.collection("pairs").document(pairId).update(mapOf(
            "userId2" to null,
            "status" to PairStatus.PENDING.name,
            "inviteKey" to null,
            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )).await()
        userRepository.updateUserSession(guestId, null, null)
        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun kickPlayer(pairId: String, kickedId: String): DomainResult<Unit> = try {
        firestore.collection("pairs").document(pairId).update(mapOf(
            "userId2" to null,
            "status" to PairStatus.PENDING.name,
            "inviteKey" to null,
            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )).await()
        userRepository.updateUserSession(kickedId, null, null)
        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    override suspend fun endSession(pairId: String): DomainResult<Unit> = try {
        val pairDoc = firestore.collection("pairs").document(pairId).get().await()
        val userId1 = pairDoc.getString("userId1")
        val userId2 = pairDoc.getString("userId2")
        firestore.collection("pairs").document(pairId).update(mapOf(
            "status" to PairStatus.ENDED.name,
            "endedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
            "userId2" to null,
            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )).await()
        if (userId1 != null) userRepository.updateUserSession(userId1, null, null)
        if (userId2 != null) userRepository.updateUserSession(userId2, null, null)
        DomainResult.Success(Unit)
    } catch (e: Throwable) {
        if (e is CancellationException) throw e
        DomainResult.Failure(mapToPairError(e))
    }

    private fun mapToPairError(error: Throwable): PairError = when (error) {
        is FirebaseFirestoreException -> when (error.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            FirebaseFirestoreException.Code.NOT_FOUND -> PairError.InvalidRequest
            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> PairError.Network(error)
            else -> PairError.Unknown
        }
        is IllegalArgumentException, is IllegalStateException -> PairError.InvalidRequest
        else -> PairError.Unknown
    }
}
