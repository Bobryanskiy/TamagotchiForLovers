package com.github.bobryanskiy.tamagotchiforlovers.data.pairing

import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model.PairData
import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model.PairModel
import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model.UserData
import com.github.bobryanskiy.tamagotchiforlovers.data.pet.model.PetState
import com.github.bobryanskiy.tamagotchiforlovers.data.storage.PetStorage
import com.github.bobryanskiy.tamagotchiforlovers.util.Result
import com.github.bobryanskiy.tamagotchiforlovers.util.Util
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
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
            val pairModel = firestore.runTransaction { transaction ->
                var pairId: String
                do {
                    pairId = Util.generatePairCode()
                } while (transaction[docRef.document(pairId)].exists())
                transaction[docRef.document(pairId)] = PairData(userId1 = currentUser)
                transaction.update(userRef, "pairId", pairId)
                PairModel(pairId, PetState())
            }.await()
            Log.d(TAG, "Successfully created new pair")
            Result.Success(pairModel)
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
            val pairResult: Result<PairModel> = firestore.runTransaction { transaction ->
                val doc = transaction[pairRef]
                var petState: PetState?
                if (doc.exists() && doc["userId2"] == "") {
                    petState = doc.toObject<PairData>()?.petState
                    transaction.update(pairRef, "userId2", currentUser)
                    transaction[userRef] = UserData(pairId)
                } else {
                    throw Exception("Pair does not exist or already has two users")
                }
                Result.Success(PairModel(pairId, petState!!))
            }.await()

            Log.d(TAG, "Successfully joined pair")
            pairResult
        } catch (e: Exception) {
            Log.d(TAG, "Error joining pair: $e")
            Result.Error(e)
        }
    }
}