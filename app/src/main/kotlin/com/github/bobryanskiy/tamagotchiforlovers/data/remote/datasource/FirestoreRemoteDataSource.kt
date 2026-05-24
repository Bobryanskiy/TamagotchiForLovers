package com.github.bobryanskiy.tamagotchiforlovers.data.remote.datasource

import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.PairDto
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.PairKeys
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.PetDto
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.PetKeys
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRemoteDataSource @Inject constructor(
    private val db: FirebaseFirestore,
    private val logger: AppLogger
) : RemoteDataSource {

    companion object {
        private const val TAG = "FirestoreDS"
        private const val MAX_RETRY_ATTEMPTS = 10L
        private const val BASE_DELAY_MS = 2_000L
        private const val MAX_DELAY_MS = 30_000L
    }

    // ────────────────────────────────────────────────────────────────
    //  Pet methods
    // ────────────────────────────────────────────────────────────────

    override fun observePet(petId: String): Flow<PetDto?> = callbackFlow {
        logger.d(TAG, "🔗 Attaching pet listener: $petId")

        val registration = db.collection("pets").document(petId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logger.w(TAG, "⚠️ Pet $petId snapshot error, closing for retry", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot?.exists() == true) {
                    trySend(snapshot.toObject(PetDto::class.java))
                } else {
                    trySend(null)
                }
            }

        awaitClose {
            registration.remove()
            logger.d(TAG, "🔌 Removed pet listener: $petId")
        }
    }.retryWhen { cause, attempt ->
        if (attempt >= MAX_RETRY_ATTEMPTS) {
            logger.e(TAG, "❌ Max retries reached for pet $petId", cause)
            return@retryWhen false
        }

        val delayMs = (BASE_DELAY_MS * (attempt + 1)).coerceAtMost(MAX_DELAY_MS)
        logger.w(TAG, "🔄 Retrying pet $petId (attempt ${attempt + 1}) in ${delayMs}ms")
        delay(delayMs)
        true  // повторяем
    }

    override suspend fun getPet(petId: String): PetDto? =
        db.collection("pets").document(petId).get().await().toObject(PetDto::class.java)

    override suspend fun upsertPet(petId: String, dto: PetDto) {
        db.collection("pets").document(petId).set(dto).await()
    }

    override suspend fun updatePetStats(
        petId: String,
        hunger: Int,
        energy: Int,
        cleanliness: Int,
        happiness: Int,
        updatedAt: Long
    ) {
        db.collection("pets").document(petId).update(
            mapOf(
                PetKeys.STATS_HUNGER to hunger,
                PetKeys.STATS_ENERGY to energy,
                PetKeys.STATS_CLEANLINESS to cleanliness,
                PetKeys.STATS_HAPPINESS to happiness,
                PetKeys.STATS_UPDATED_AT to updatedAt
            )
        ).await()
    }

    override suspend fun updatePetLifeState(
        petId: String,
        status: String,
        isBlocked: Boolean,
        multiplier: Float,
        recoveryTime: Long?,
        updatedAt: Long
    ) {
        db.collection("pets").document(petId).update(
            mapOf(
                PetKeys.LIFE_STATUS to status,
                PetKeys.LIFE_IS_ACTIONS_BLOCKED to isBlocked,
                PetKeys.LIFE_DECAY_MULTIPLIER to multiplier,
                PetKeys.LIFE_RECOVERY_END_TIME to recoveryTime,
                PetKeys.STATS_UPDATED_AT to updatedAt
            )
        ).await()
    }

    override suspend fun updatePetPairId(petId: String, pairId: String?, updatedAt: Long) {
        db.collection("pets").document(petId).update(
            mapOf(
                PetKeys.PROFILE_CURRENT_PAIR_ID to pairId,
                PetKeys.STATS_UPDATED_AT to updatedAt
            )
        ).await()
    }

    override suspend fun updatePetName(petId: String, name: String, updatedAt: Long) {
        db.collection("pets").document(petId).update(
            mapOf(
                PetKeys.PROFILE_NAME to name,
                PetKeys.STATS_UPDATED_AT to updatedAt
            )
        ).await()
    }

    override suspend fun deletePet(petId: String) {
        db.collection("pets").document(petId).delete().await()
    }

    override suspend fun getPetsByOwner(ownerId: String): List<kotlin.Pair<String, PetDto>> {
        val snapshot = db.collection("pets")
            .whereEqualTo(PetKeys.PROFILE_OWNER_USER_ID, ownerId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(PetDto::class.java)?.let { doc.id to it }
        }
    }

    override suspend fun batchMigrateOwnerUserId(oldOwnerId: String?, newOwnerId: String) {
        val batch = db.batch()
        val query = if (oldOwnerId == null) {
            db.collection("pets").whereEqualTo(PetKeys.PROFILE_OWNER_USER_ID, "")
        } else {
            db.collection("pets").whereEqualTo(PetKeys.PROFILE_OWNER_USER_ID, oldOwnerId)
        }

        query.get().await().documents.forEach { doc ->
            batch.update(doc.reference, PetKeys.PROFILE_OWNER_USER_ID, newOwnerId)
            batch.update(doc.reference, PetKeys.STATS_UPDATED_AT, System.currentTimeMillis())
        }
        batch.commit().await()
    }

    // ────────────────────────────────────────────────────────────────
    //  Pair methods
    // ────────────────────────────────────────────────────────────────

    override fun observePair(pairId: String): Flow<PairDto?> = callbackFlow {
        logger.d(TAG, "🔗 Attaching pair listener: $pairId")

        val registration = db.collection("pairs").document(pairId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logger.w(TAG, "⚠️ Pair $pairId snapshot error, closing for retry", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot?.exists() == true) {
                    try {
                        val dto = snapshot.toObject(PairDto::class.java)
                        logger.d(TAG, "📦 Pair update: status=${dto?.status}, userId2=${dto?.userId2}")
                        trySend(dto)
                    } catch (e: Exception) {
                        logger.e(TAG, "Error parsing PairDto", e)
                    }
                } else {
                    logger.d(TAG, "📦 Pair deleted: $pairId")
                    trySend(null)
                }
            }

        awaitClose {
            registration.remove()
            logger.d(TAG, "🔌 Removed pair listener: $pairId")
        }
    }.retryWhen { cause, attempt ->
        if (attempt >= MAX_RETRY_ATTEMPTS) {
            logger.e(TAG, "❌ Max retries for pair $pairId", cause)
            return@retryWhen false
        }
        val delayMs = (BASE_DELAY_MS * (attempt + 1)).coerceAtMost(MAX_DELAY_MS)
        logger.w(TAG, "🔄 Retrying pair $pairId (attempt ${attempt + 1})")
        delay(delayMs)
        true
    }


    override suspend fun getPair(pairId: String): PairDto? =
        db.collection("pairs").document(pairId).get().await().toObject(PairDto::class.java)

    override suspend fun upsertPair(pairId: String, dto: PairDto) {
        db.collection("pairs").document(pairId).set(dto).await()
    }

    override suspend fun updatePairStatus(pairId: String, status: String, updatedAt: Long) {
        db.collection("pairs").document(pairId).update(
            mapOf(PairKeys.STATUS to status, PairKeys.UPDATED_AT to updatedAt)
        ).await()
    }

    override suspend fun updatePairUserId2(pairId: String, userId2: String?, updatedAt: Long) {
        db.collection("pairs").document(pairId).update(
            mapOf(PairKeys.USER_ID_2 to userId2, PairKeys.UPDATED_AT to updatedAt)
        ).await()
    }

    override suspend fun deletePair(pairId: String) {
        db.collection("pairs").document(pairId).delete().await()
    }

    override suspend fun findPairByInviteKey(inviteKey: String): PairDto? {
        val snapshot = db.collection("pairs")
            .whereEqualTo("invite_key.code", inviteKey.uppercase())
            .limit(1)
            .get()
            .await()

        if (snapshot.isEmpty) return null
        return snapshot.documents.first().toObject(PairDto::class.java)
    }

    override suspend fun requestJoin(pairId: String, guestId: String) {
        db.collection("pairs").document(pairId)
            .update(
                "pending_request", mapOf(
                    "guest_id" to guestId,
                    "requested_at" to FieldValue.serverTimestamp()
                )
            )
            .await()
    }

    override suspend fun acceptJoinRequest(pairId: String, guestId: String) {
        db.runTransaction { transaction ->
            val ref = db.collection("pairs").document(pairId)
            transaction.get(ref) // проверка существования

            transaction.update(ref, "user_id_2", guestId)
            transaction.update(ref, "status", "ACTIVE")
            transaction.update(ref, "pending_request", FieldValue.delete())
            transaction.update(ref, "invite_key", FieldValue.delete())
            transaction.update(ref, "updated_at", System.currentTimeMillis())
        }.await()
    }

    override suspend fun rejectJoinRequest(pairId: String, guestId: String) {
        db.collection("pairs").document(pairId)
            .update("pending_request", FieldValue.delete())
            .await()
    }

    override suspend fun leaveSession(pairId: String, userId: String) {
        db.collection("pairs").document(pairId)
            .update(
                "user_id_2", FieldValue.delete(),
                "status", "PENDING",
                "invite_key", FieldValue.delete(),
                "pending_request", FieldValue.delete(),
                "updated_at", System.currentTimeMillis()
            )
            .await()
    }

    override suspend fun endSession(pairId: String, callerId: String) {
        db.collection("pairs").document(pairId)
            .update(
                "status", "ENDED",
                "ended_at", FieldValue.serverTimestamp(),
                "user_id_2", FieldValue.delete(),
                "invite_key", FieldValue.delete(),
                "pending_request", FieldValue.delete(),
                "updated_at", System.currentTimeMillis()
            )
            .await()
    }

    override suspend fun kickPartner(pairId: String, callerId: String) {
        db.collection("pairs").document(pairId)
            .update(
                "user_id_2", FieldValue.delete(),
                "status", "PENDING",
                "invite_key", FieldValue.delete(),
                "pending_request", FieldValue.delete(),
                "updated_at", System.currentTimeMillis()
            )
            .await()
    }

    override suspend fun generateInviteKey(pairId: String, code: String, expiresAt: Long) {
        db.collection("pairs").document(pairId)
            .update(
                "invite_key", mapOf(
                    "code" to code,
                    "expires_at" to expiresAt
                )
            )
            .await()
    }

    override fun observePendingRequests(pairId: String): Flow<List<Map<String, Any?>>> = callbackFlow {
        logger.d(TAG, "🔍 Observing pending requests for pair: $pairId")

        val registration = db.collection("pairs").document(pairId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logger.w(TAG, "⚠️ Pending requests error", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val requestData = snapshot.get("pending_request") as? Map<*, *>
                val list = if (requestData?.get("guest_id") is String) {
                    listOf(requestData.mapKeys { (key, _) -> key.toString() })
                } else emptyList()

                trySend(list)
            }

        awaitClose {
            registration.remove()
            logger.d(TAG, "🔌 Removed pending requests listener: $pairId")
        }
    }.retryWhen { cause, attempt ->
        if (attempt >= MAX_RETRY_ATTEMPTS) return@retryWhen false
        val delayMs = (BASE_DELAY_MS * (attempt + 1)).coerceAtMost(MAX_DELAY_MS)
        logger.w(TAG, "🔄 Retrying pending requests for $pairId")
        delay(delayMs)
        true
    }
}