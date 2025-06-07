package com.github.bobryanskiy.tamagotchiforlovers.data

import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.PairData
import com.github.bobryanskiy.tamagotchiforlovers.PetState
import com.github.bobryanskiy.tamagotchiforlovers.util.Util.generatePairCode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class PairRepository {

    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = Firebase.firestore

    fun createNewPair(callback: (String?) -> Unit) {
        Log.d("pair", "Started")
        val currentUser = auth.currentUser?.uid ?: return
        Log.d("pair", currentUser)
        val docRef = firestore.collection("pairs")
        firestore.runTransaction { transaction ->
            var pairId: String
            do {
                pairId = generatePairCode()
            } while (transaction.get(docRef.document(pairId)).exists())
            Log.d("pair", "Started2")
            transaction.set(docRef.document(pairId), PairData(userId1 = currentUser))
            pairId
        }.addOnSuccessListener { pairId ->
            callback(pairId)
            Log.d("pair", "Successfully created new pair")
        }.addOnFailureListener { exception ->
            callback(null)
            Log.d("pair", "Error creating new pair: $exception")
        }
    }

    fun findExistingPair(callback: (String?) -> Unit) {
        firestore.collection("pairs")
            .whereEqualTo("isActive", false)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    callback(null)
                } else {
                    callback(documents.first().id)
                }
            }
    }

    fun joinPair(pairId: String, callback: () -> Unit) {
        val currentUser = auth.currentUser?.uid ?: return
        firestore.collection("pairs").document(pairId)
            .update("userId2", currentUser, "isActive", true)
            .addOnSuccessListener { callback() }
    }

    fun observePetState(pairId: String, callback: (PetState) -> Unit) {
        firestore.collection("pairs").document(pairId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val pairData = snapshot.toObject<PairData>()
                    callback(pairData!!.petState)
                }
            }
    }

    fun feedPet(pairId: String) {
        firestore.collection("pairs").document(pairId)
            .get()
            .addOnSuccessListener { snapshot ->
                val pairData = snapshot.toObject<PairData>() ?: return@addOnSuccessListener
                val newPetState = pairData.petState.copy(hunger = 100)
                firestore.collection("pairs").document(pairId)
                    .update("petState", newPetState)
            }
    }
}