package com.github.bobryanskiy.tamagotchiforlovers.data.login

import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.data.login.model.LoggedInUser
import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model.PairData
import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model.UserData
import com.github.bobryanskiy.tamagotchiforlovers.data.pet.model.PetState
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
            val doc = firestore.collection("users").document(user.uid)
            val doc2 = firestore.collection("pairs")
            val transactionData = firestore.runTransaction { transaction ->
                val userData = transaction[doc].toObject<UserData>()
                if (userData == null) throw Exception("User data is null")
                val pairData = transaction[doc2.document(userData.pairId)].toObject<PairData>()
                Pair(pairData, userData)
            }.await()
            Result.Success(LoggedInUser(user.uid, transactionData.second.pairId,
                transactionData.first?.petState ?: PetState(), user.displayName))
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
            Result.Success(LoggedInUser(user.uid, "", PetState(), user.displayName))
        } catch (e: Exception) {
            Log.e("EmailPassword", "createAccountWithEmail:failure", e)
            Result.Error(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}