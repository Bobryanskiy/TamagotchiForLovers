package com.github.bobryanskiy.tamagotchiforlovers.data.repository

import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.data.model.dto.UserDto
import com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper.toDomain
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.User
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {
    override fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    override suspend fun createUser(uid: String) {
        firestore.collection("users").document(uid)
            .set(UserDto(uid = uid, createdAt = System.currentTimeMillis()))
            .await()
    }

    override suspend fun updateUserSession(uid: String, petId: String?, pairId: String?) {
        val updates = mutableMapOf<String, Any?>()
        if (petId != null) updates["activePetId"] = petId else updates["activePetId"] = null
        if (pairId != null) updates["activePairId"] = pairId else updates["activePairId"] = null

        firestore.collection("users").document(uid)
            .set(updates, com.google.firebase.firestore.SetOptions.merge())
            .await()
    }

    override fun observeUser(uid: String): Flow<User?> {
        return firestore.collection("users").document(uid)
            .snapshots()
            .map { it.toObject(UserDto::class.java)?.toDomain() }
            .catch { e -> Log.e("TAMAGOTCHI", "",e) }
    }
}