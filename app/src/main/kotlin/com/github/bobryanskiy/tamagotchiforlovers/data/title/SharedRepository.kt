package com.github.bobryanskiy.tamagotchiforlovers.data.title

import android.util.Log
import com.github.bobryanskiy.tamagotchiforlovers.data.pet.model.PetState
import com.github.bobryanskiy.tamagotchiforlovers.data.storage.PetStorage
import com.github.bobryanskiy.tamagotchiforlovers.data.title.model.ResultListener
import com.github.bobryanskiy.tamagotchiforlovers.util.Result
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SharedRepository(private val petStorage: PetStorage) {
    private val firestore: FirebaseFirestore = Firebase.firestore

    fun subscribeToFirestore(pairId: String): Result<ResultListener> {
        return try {
            val petDocument = firestore.collection("pets").document(pairId)
            var petState: PetState = petStorage.getPetState()
            petDocument.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firestore", "Listener Error", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val updatedState = snapshot.toObject(PetState::class.java)
                    updatedState?.let {
                        petState = it
                        saveToSharedPreferences(it)
                    }
                }
            }
            Result.Success(ResultListener(petState))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun saveToSharedPreferences(petState: PetState) {
        petStorage.savePetState(petState)
    }
}