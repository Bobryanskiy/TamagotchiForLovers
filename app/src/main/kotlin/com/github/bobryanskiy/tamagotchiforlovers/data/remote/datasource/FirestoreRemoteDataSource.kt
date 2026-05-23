package com.github.bobryanskiy.tamagotchiforlovers.data.remote.datasource

import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.PairDto
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.PairKeys
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.PetDto
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.PetKeys
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRemoteDataSource @Inject constructor(
    private val db: FirebaseFirestore
) : RemoteDataSource {
    // Pet methods
    override fun observePet(petId: String): Flow<PetDto?> = callbackFlow {
        val listener = db.collection("pets")
            .document(petId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val dto = snapshot.toObject(PetDto::class.java)
                        trySend(dto)
                    } catch (e: Exception) {
                        close(e)
                    }
                } else {
                    trySend(null)
                }
            }

        awaitClose { listener.remove() }
    }
    override suspend fun getPet(petId: String): PetDto? = db.collection("pets").document(petId).get().await().toObject(PetDto::class.java)
    override suspend fun upsertPet(petId: String, dto: PetDto) { db.collection("pets").document(petId).set(dto).await() }
    override suspend fun updatePetStats(petId: String, hunger: Int, energy: Int, cleanliness: Int, happiness: Int, updatedAt: Long) {
        db.collection("pets").document(petId).update(mapOf(PetKeys.STATS_HUNGER to hunger, PetKeys.STATS_ENERGY to energy,
            PetKeys.STATS_CLEANLINESS to cleanliness, PetKeys.STATS_HAPPINESS to happiness, PetKeys.STATS_UPDATED_AT to updatedAt)).await()
    }
    override suspend fun updatePetLifeState(petId: String, status: String, isBlocked: Boolean, multiplier: Float, recoveryTime: Long?, updatedAt: Long) {
        db.collection("pets").document(petId).update(mapOf(PetKeys.LIFE_STATUS to status, PetKeys.LIFE_IS_ACTIONS_BLOCKED to isBlocked,
            PetKeys.LIFE_DECAY_MULTIPLIER to multiplier, PetKeys.LIFE_RECOVERY_END_TIME to recoveryTime, PetKeys.STATS_UPDATED_AT to updatedAt)).await()
    }
    override suspend fun updatePetPairId(petId: String, pairId: String?, updatedAt: Long) {
        db.collection("pets").document(petId).update(mapOf(PetKeys.PROFILE_CURRENT_PAIR_ID to pairId, PetKeys.STATS_UPDATED_AT to updatedAt)).await()
    }
    override suspend fun updatePetName(petId: String, name: String, updatedAt: Long) {
        db.collection("pets").document(petId).update(mapOf(PetKeys.PROFILE_NAME to name, PetKeys.STATS_UPDATED_AT to updatedAt)).await()
    }
    override suspend fun deletePet(petId: String) { db.collection("pets").document(petId).delete().await() }
    override suspend fun getPetsByOwner(ownerId: String): List<Pair<String, PetDto>> {
        val snapshot = db.collection("pets").whereEqualTo(PetKeys.PROFILE_OWNER_USER_ID, ownerId).get().await()
        return snapshot.documents.mapNotNull { doc -> doc.toObject(PetDto::class.java)?.let { doc.id to it } }
    }
    override suspend fun batchMigrateOwnerUserId(oldOwnerId: String?, newOwnerId: String) {
        val batch = db.batch()
        val query = if (oldOwnerId == null) db.collection("pets").whereEqualTo(PetKeys.PROFILE_OWNER_USER_ID, "") else db.collection("pets").whereEqualTo(PetKeys.PROFILE_OWNER_USER_ID, oldOwnerId)
        query.get().await().documents.forEach { doc ->
            batch.update(doc.reference, PetKeys.PROFILE_OWNER_USER_ID, newOwnerId)
            batch.update(doc.reference, PetKeys.STATS_UPDATED_AT, System.currentTimeMillis())
        }
        batch.commit().await()
    }

    // Pair methods
    override fun observePair(pairId: String): Flow<PairDto?> = callbackFlow {
        Log.d("FIREBASE_DEBUG", "🔗 [RemoteDataSource] Attaching listener for pair: $pairId")

        val listener = db.collection("pairs")
            .document(pairId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FIREBASE_DEBUG", "❌ [RemoteDataSource] Error listening to pair $pairId: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val dto = snapshot.toObject(PairDto::class.java)
                        Log.d("FIREBASE_DEBUG", "📦 [RemoteDataSource] Received from Firestore: id=${snapshot.id}, status=${dto?.status}, user_id_2=${dto?.userId2}")
                        trySend(dto)
                    } catch (e: Exception) {
                        Log.e("FIREBASE_DEBUG", "❌ [RemoteDataSource] Error parsing PairDto: ${e.message}", e)
                        close(e)
                    }
                } else {
                    Log.d("FIREBASE_DEBUG", "📦 [RemoteDataSource] Document does not exist or was deleted: $pairId")
                    trySend(null)
                }
            }

        awaitClose {
            listener.remove()
            Log.d("FIREBASE_DEBUG", "🔌 [RemoteDataSource] Removed listener for pair: $pairId")
        }
    }
    override suspend fun getPair(pairId: String): PairDto? = db.collection("pairs").document(pairId).get().await().toObject(PairDto::class.java)
    override suspend fun upsertPair(pairId: String, dto: PairDto) { db.collection("pairs").document(pairId).set(dto).await() }
    override suspend fun updatePairStatus(pairId: String, status: String, updatedAt: Long) {
        db.collection("pairs").document(pairId).update(mapOf(PairKeys.STATUS to status, PairKeys.UPDATED_AT to updatedAt)).await()
    }
    override suspend fun updatePairUserId2(pairId: String, userId2: String?, updatedAt: Long) {
        db.collection("pairs").document(pairId).update(mapOf(PairKeys.USER_ID_2 to userId2,
            PairKeys.UPDATED_AT to updatedAt)).await()
    }
    override suspend fun deletePair(pairId: String) { db.collection("pairs").document(pairId).delete().await() }
    override suspend fun findPairByInviteKey(inviteKey: String): PairDto? {
        val snapshot = db.collection("pairs")
            .whereEqualTo("invite_key.code", inviteKey.uppercase())
            .limit(1)
            .get()
            .await()

        if (snapshot.isEmpty) return null

        val doc = snapshot.documents.first()
        return doc.toObject(PairDto::class.java)
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
            val snapshot = transaction.get(ref)

            transaction.update(ref, "user_id_2", guestId)
            transaction.update(ref, "status", "ACTIVE")
            transaction.update(ref, "pending_request", null)
            transaction.update(ref, "updated_at", System.currentTimeMillis())
        }.await()
    }

    override suspend fun rejectJoinRequest(pairId: String, guestId: String) {
        db.collection("pairs").document(pairId)
            .update("pending_request", null)
            .await()
    }

    override suspend fun leaveSession(pairId: String, userId: String) {
        db.collection("pairs").document(pairId)
            .update("user_id_2", null, "status", "PENDING", "updated_at", System.currentTimeMillis())
            .await()
    }

    override suspend fun endSession(pairId: String, callerId: String) {
        db.collection("pairs").document(pairId)
            .update("status", "ENDED", "ended_at", FieldValue.serverTimestamp(), "updated_at", System.currentTimeMillis())
            .await()
    }

    override suspend fun kickPartner(pairId: String, callerId: String) {
        db.collection("pairs").document(pairId)
            .update("user_id_2", null, "status", "PENDING", "updated_at", System.currentTimeMillis())
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
        Log.d("FIREBASE_DEBUG", "🔍 [observePendingRequests] Starting observation for pair: $pairId")

        val registration = db.collection("pairs").document(pairId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FIREBASE_DEBUG", "❌ [observePendingRequests] Error: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    Log.d("FIREBASE_DEBUG", "⚠️ [observePendingRequests] Pair document doesn't exist: $pairId")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val requestData = snapshot.get("pending_request") as? Map<*, *>
                Log.d("FIREBASE_DEBUG", "📋 [observePendingRequests] Raw pending_request  $requestData")

                val list = if (requestData != null && requestData["guest_id"] != null) {
                    listOf(requestData)
                } else {
                    emptyList()
                }
//                trySend(list)
            }

        awaitClose {
            registration.remove()
            Log.d("FIREBASE_DEBUG", "🔌 [observePendingRequests] Removed listener for pair: $pairId")
        }
    }
}