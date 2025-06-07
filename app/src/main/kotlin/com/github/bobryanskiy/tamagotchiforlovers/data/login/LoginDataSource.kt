package com.github.bobryanskiy.tamagotchiforlovers.data.login

import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.data.login.model.LoggedInUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {
    private val auth: FirebaseAuth = Firebase.auth

    suspend fun loginViaEmail(username: String, password: String): Result<LoggedInUser> {
        return try {
            val task = auth.signInWithEmailAndPassword(username, password).await()
            val user = task.user!!
            Log.d("EmailPassword", "signInWithEmail:success")
            Result.Success(LoggedInUser(user.uid, user.displayName))
        } catch (e: Exception) {
            Log.w("EmailPassword", "signInWithEmail:failure", e)
            Result.Error(e)
        }
    }

    suspend fun registerViaEmail(username: String, password: String): Result<LoggedInUser> {
        return try {
            Log.d("EmailPassword", "createAccountInWithEmail:success")
            val task = auth.createUserWithEmailAndPassword(username, password).await()
            val user = task.user!!
            Result.Success(LoggedInUser(user.uid, user.displayName))
        } catch (e: Exception) {
            Log.w("EmailPassword", "createAccountWithEmail:failure", e)
            Result.Error(e)
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}