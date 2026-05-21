package com.github.bobryanskiy.tamagotchiforlovers.data.repository

import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.UserError
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {
    override fun getCurrentUserId(): String? = auth.currentUser?.uid
    override fun getCurrentUserEmail(): String? = auth.currentUser?.email
    override fun isLoggedIn(): Boolean = auth.currentUser != null

    override suspend fun signIn(email: String, password: String): DomainResult<Unit> = try {
        auth.signInWithEmailAndPassword(email, password).await()
        DomainResult.Success(Unit)
    } catch (e: Exception) {
        DomainResult.Failure(PetError.Network)
    }

    override suspend fun signUp(email: String, password: String): DomainResult<Unit> = try {
        auth.createUserWithEmailAndPassword(email, password).await()
        DomainResult.Success(Unit)
    } catch (e: FirebaseAuthUserCollisionException) {
        DomainResult.Failure(UserError.EmailAlreadyExists)
    } catch (e: FirebaseAuthWeakPasswordException) {
        DomainResult.Failure(UserError.WeakPassword)
    } catch (e: Exception) {
        Log.e("AuthRepository", "Sign up failed", e)
        DomainResult.Failure(PetError.Network)
    }

    override suspend fun signOut(): DomainResult<Unit> = try {
        auth.signOut()
        DomainResult.Success(Unit)
    } catch (e: Exception) {
        DomainResult.Failure(PetError.Unknown)
    }
}