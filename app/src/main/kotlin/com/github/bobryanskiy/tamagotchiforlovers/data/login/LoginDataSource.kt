package com.github.bobryanskiy.tamagotchiforlovers.data.login

import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.data.login.model.LoggedInUser
import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model.UserData
import com.github.bobryanskiy.tamagotchiforlovers.util.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = Firebase.firestore

    suspend fun loginViaEmail(username: String, password: String): Result<LoggedInUser> {
        return try {
            val task = auth.signInWithEmailAndPassword(username, password).await()
            val user = task.user!!
            Log.d("EmailPassword", "signInWithEmail:success")
            val doc = firestore.collection("users").document(user.uid).get().await()
            // TODO : СДЕЛАТЬ ПОЛУЧЕНИЕ ДАННЫХ ИЗ ПАРЫ - транзакция
            val userData = doc.toObject<UserData>()
            val code = userData!!.pairId
            Result.Success(LoggedInUser(user.uid, code, user.displayName))
        } catch (e: Exception) {
            Log.e("EmailPassword", "signInWithEmail:failure", e)
            Result.Error(e)
        }
    }

    suspend fun registerViaEmail(username: String, password: String): Result<LoggedInUser> {
        return try {
            Log.d("EmailPassword", "createAccountInWithEmail:success")
            val task = auth.createUserWithEmailAndPassword(username, password).await()
            val user = task.user!!
            firestore.collection("users").document(user.uid).set(UserData()).await()
            Result.Success(LoggedInUser(user.uid, null, user.displayName))
        } catch (e: Exception) {
            Log.e("EmailPassword", "createAccountWithEmail:failure", e)
            Result.Error(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}