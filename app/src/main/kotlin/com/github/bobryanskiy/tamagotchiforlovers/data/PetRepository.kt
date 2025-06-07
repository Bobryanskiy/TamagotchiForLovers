package com.github.bobryanskiy.tamagotchiforlovers.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.bobryanskiy.tamagotchiforlovers.PairData
import com.github.bobryanskiy.tamagotchiforlovers.PetState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class PetRepository {
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val _petState = MutableLiveData<PetState>()
    val petState: LiveData<PetState> = _petState

    fun observePetState(pairId: String) {
        firestore.collection("pairs").document(pairId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    val pairData = snapshot.toObject<PairData>()
                    _petState.postValue(pairData?.petState ?: PetState())
                }
            }
    }

    fun updatePetState(pairId: String, newPetState: PetState) {
        firestore.collection("pairs").document(pairId)
            .update("petState", newPetState)
    }
}