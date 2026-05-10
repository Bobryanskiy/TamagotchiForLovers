package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

import com.github.bobryanskiy.tamagotchiforlovers.util.ValidationUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginUseCase @Inject constructor(
    private val auth: FirebaseAuth
) {
    suspend operator fun invoke(email: String, password: String): Result<String> {
        // Валидация входных данных
        ValidationUtils.getEmailError(email)?.let {
            return Result.failure(Exception(it))
        }
        ValidationUtils.getPasswordError(password)?.let {
            return Result.failure(Exception(it))
        }

        return try {
            val signInResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = signInResult.user?.uid ?: return Result.failure(Exception("User ID is null"))
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}