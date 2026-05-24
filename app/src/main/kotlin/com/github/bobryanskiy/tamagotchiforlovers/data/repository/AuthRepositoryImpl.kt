package com.github.bobryanskiy.tamagotchiforlovers.data.repository

import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.dto.UserDto
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.UserError
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.UserResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val clock: Clock,
    private val logger: AppLogger
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
        private const val USERS_COLLECTION = "users"
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid
    override fun getCurrentUserEmail(): String? = auth.currentUser?.email
    override fun isLoggedIn(): Boolean = auth.currentUser != null

    override suspend fun signIn(email: String, password: String): UserResult<Unit> = try {
        auth.signInWithEmailAndPassword(email, password).await()
        logger.d(TAG, "✅ User signed in: $email")
        DomainResult.Success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logger.w(TAG, "Sign in failed for $email", e)
        DomainResult.Failure(UserError.LoginError)
    }

    override suspend fun signUp(email: String, password: String): UserResult<Unit> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw IllegalStateException("User created but uid is null")

        createUserDocument(uid, email)

        logger.d(TAG, "✅ User signed up and document created: $uid")
        DomainResult.Success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: FirebaseAuthUserCollisionException) {
        logger.w(TAG, "Sign up failed: email already exists: $email")
        DomainResult.Failure(UserError.EmailAlreadyExists)
    } catch (e: FirebaseAuthWeakPasswordException) {
        logger.w(TAG, "Sign up failed: weak password for $email")
        DomainResult.Failure(UserError.WeakPassword)
    } catch (e: Exception) {
        logger.e(TAG, "Sign up failed for $email", e)
        DomainResult.Failure(UserError.Unknown)
    }

    override suspend fun signOut(): UserResult<Unit> = try {
        auth.signOut()
        logger.d(TAG, "✅ User signed out")
        DomainResult.Success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logger.e(TAG, "Sign out failed", e)
        DomainResult.Failure(UserError.Unknown)
    }

    /**
     * Создаёт документ пользователя в Firestore.
     *
     * ⚠️ Важно: вызывается только после успешной регистрации в Firebase Auth.
     * Без этого шага observeCurrentUser() вернёт null, ProfileScreen будет пустым.
     *
     * Используем merge() для идемпотентности — если документ уже существует,
     * не перезаписываем существующие данные (например nickname).
     */
    private suspend fun createUserDocument(uid: String, email: String?) {
        val userDto = UserDto(
            uid = uid,
            email = email,
            nickname = null,
            activePetId = null,
            activePairId = null,
            createdAt = clock.currentTimeMillis()
        )
        firestore.collection(USERS_COLLECTION).document(uid)
            .set(userDto, SetOptions.merge())
            .await()
    }
}