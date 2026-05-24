package com.github.bobryanskiy.tamagotchiforlovers.data.repository

import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.data.model.mapper.toDomain
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.UserDto
import com.github.bobryanskiy.tamagotchiforlovers.di.IoDispatcher
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.UserError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.User
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.UserResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val clock: Clock,
    private val logger: AppLogger,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserRepository {

    companion object {
        private const val TAG = "UserRepository"
        private const val USERS_COLLECTION = "users"
    }

    /**
     * Наблюдает за текущим пользователем.
     *
     * ⚠️ Если пользователь не авторизован при вызове — вернёт Flow<null>.
     * Для реактивной обработки смены auth state используйте FirebaseAuthStateListener.
     */
    override fun observeCurrentUser(): Flow<User?> {
        val uid = auth.currentUser?.uid ?: return flow { emit(null) }

        return firestore.collection(USERS_COLLECTION).document(uid)
            .snapshots()
            .map { snapshot ->
                snapshot.toObject(UserDto::class.java)?.toDomain()
            }
            .catch { e ->
                logger.e(TAG, "Error observing user $uid", e)
                emit(null)
            }
            .flowOn(ioDispatcher + CoroutineName("UserRepo"))
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Создаёт документ пользователя в Firestore.
     *
     * Использует merge() чтобы не перезаписать существующие данные
     * (например если документ был создан другим процессом).
     */
    override suspend fun createUser(uid: String) {
        val userDto = UserDto(
            uid = uid,
            email = auth.currentUser?.email,  // ← сохраняем email при создании
            createdAt = clock.currentTimeMillis()
        )
        firestore.collection(USERS_COLLECTION).document(uid)
            .set(userDto, SetOptions.merge())
            .await()
        logger.d(TAG, "✅ User $uid created/updated")
    }

    /**
     * Обновляет активные petId и pairId в сессии пользователя.
     *
     * Использует FieldValue.delete() для корректного удаления полей
     * вместо установки в null (которое может вызвать проблемы с индексами).
     */
    override suspend fun updateUserSession(uid: String, petId: String?, pairId: String?) {
        val updates = mutableMapOf<String, Any>()

        updates["active_pet_id"] = petId ?: FieldValue.delete()
        updates["active_pair_id"] = pairId ?: FieldValue.delete()
        updates["updated_at"] = clock.currentTimeMillis()

        firestore.collection(USERS_COLLECTION).document(uid)
            .set(updates, SetOptions.merge())
            .await()

        logger.d(TAG, "✅ User session updated: petId=$petId, pairId=$pairId")
    }

    /**
     * Наблюдает за конкретным пользователем по uid.
     *
     * Используется для отображения ников гостей в PairWaitingScreen.
     */
    override fun observeUser(uid: String): Flow<User?> {
        return firestore.collection(USERS_COLLECTION).document(uid)
            .snapshots()
            .map { it.toObject(UserDto::class.java)?.toDomain() }
            .catch { e ->
                logger.e(TAG, "Error observing user $uid", e)
                emit(null)
            }
            .flowOn(ioDispatcher + CoroutineName("UserRepo-$uid"))
    }

    override suspend fun updateNickname(uid: String, nickname: String): UserResult<Unit> = try {
        firestore.collection(USERS_COLLECTION).document(uid)
            .update(
                mapOf(
                    "nickname" to nickname,
                    "updated_at" to clock.currentTimeMillis()
                )
            )
            .await()
        logger.d(TAG, "✅ Nickname updated for $uid: $nickname")
        DomainResult.Success(Unit)
    } catch (e: Exception) {
        logger.e(TAG, "Failed to update nickname for $uid", e)
        DomainResult.Failure(UserError.Unknown)
    }

    override suspend fun getUserNickname(uid: String): String? = try {
        val doc = firestore.collection(USERS_COLLECTION).document(uid).get().await()
        doc.getString("nickname")
    } catch (e: Exception) {
        logger.w(TAG, "Failed to get nickname for $uid", e)
        null
    }
}