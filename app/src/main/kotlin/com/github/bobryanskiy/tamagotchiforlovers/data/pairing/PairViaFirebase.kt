package com.github.bobryanskiy.tamagotchiforlovers.data.pairing

import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model.PairData
import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model.PairModel
import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model.UserData
import com.github.bobryanskiy.tamagotchiforlovers.util.Result
import com.github.bobryanskiy.tamagotchiforlovers.util.Util
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

private const val TAG = "Tamagotchi.Pairing"

class PairViaFirebase {
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = Firebase.firestore

    suspend fun createNewPair(): Result<PairModel> {
        return try {
            val currentUser = auth.currentUser!!.uid
            val docRef = firestore.collection("pairs")
            val userRef = firestore.collection("users").document(currentUser)
            val pairId = firestore.runTransaction { transaction ->
                var pairId: String
                do {
                    pairId = Util.generatePairCode()
                } while (transaction[docRef.document(pairId)].exists())
                transaction[docRef.document(pairId)] = PairData(userId1 = currentUser)
                transaction.update(userRef, "pairId", pairId)
                pairId
            }.await()
            Log.d(TAG, "Successfully created new pair")
            Result.Success(PairModel(pairId))
        } catch (e: Exception) {
            Log.d(TAG, "Error creating new pair: $e")
            Result.Error(e)
        }
    }


    suspend fun joinPair(pairId: String): Result<PairModel> {
        return try {
            val currentUser = auth.currentUser!!.uid
            val pairRef = firestore.collection("pairs").document(pairId)
            val userRef = firestore.collection("users").document(currentUser)
            firestore.runTransaction { transaction ->
                val doc = transaction[pairRef]
                if (doc.exists() && doc["userId2"] == null) {
                    transaction.update(pairRef, "userId2", currentUser)
                    transaction[userRef] = UserData(pairId)
                } else {
                    Result.Error(Exception("Pair does not exist or already has two users"))
                }
            }.await()
            Log.d(TAG, "Successfully joined pair")
            Result.Success(PairModel(pairId))
        } catch (e: Exception) {
            Log.d(TAG, "Error joining pair: $e")
            Result.Error(e)
        }
    }
}