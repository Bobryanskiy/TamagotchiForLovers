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

private const val TAG = "Tamagotchi.Pairing"

class PairViaFirebase {
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = Firebase.firestore

    fun createNewPair(callback: (Result<PairModel>) -> Unit) {
        val currentUser = auth.currentUser?.uid ?: return
        val docRef = firestore.collection("pairs")
        firestore.runTransaction { transaction ->
            var pairId: String
            do {
                pairId = Util.generatePairCode()
            } while (transaction[docRef.document(pairId)].exists())
            transaction[docRef.document(pairId)] = PairData(userId1 = currentUser)
            transaction.update(firestore.collection("users").document(currentUser), "pairId",pairId)
            pairId
        }.addOnSuccessListener { pairId ->
            Log.d(TAG, "Successfully created new pair")
            callback(Result.Success(PairModel(pairId)))
        }.addOnFailureListener { exception ->
            Log.d(TAG, "Error creating new pair: $exception")
            callback(Result.Error(exception))
        }
    }


    fun joinPair(pairId: String, callback: (Result<PairModel>) -> Unit) {
        val currentUser = auth.currentUser?.uid ?: return
        val docRef = firestore.collection("pairs").document(pairId)
        firestore.runTransaction { transaction ->
            if (transaction[docRef].exists() && transaction[docRef]["userId2"] == null) {
                transaction.update(docRef,"userId2",currentUser)
            }
            transaction[firestore.collection("users").document(currentUser)] = UserData(pairId)
        }.addOnSuccessListener {
            Log.d(TAG, "Successfully joined pair")
            callback(Result.Success(PairModel(pairId)))
        }.addOnFailureListener { exception ->
            Log.d(TAG, "Error joining pair: $exception")
            callback(Result.Error(exception))
        }
    }
}