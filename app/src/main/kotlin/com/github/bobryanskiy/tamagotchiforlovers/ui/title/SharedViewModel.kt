package com.github.bobryanskiy.tamagotchiforlovers.ui.title

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model.PairData
import com.github.bobryanskiy.tamagotchiforlovers.data.pet.model.PetState
import com.github.bobryanskiy.tamagotchiforlovers.data.title.SharedRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class SharedViewModel(private val repository: SharedRepository) : ViewModel() {
    private val _petState = MutableLiveData<PetState>()
    val petState: LiveData<PetState> = _petState

    fun subscribeToFirestore(pairId: String) {
        val petDocument = Firebase.firestore.collection("pairs").document(pairId)
        petDocument.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("Firestore", "Listener Error", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d("Firestore", "Current data: ${snapshot.data}")
                val updatedState = snapshot.toObject<PairData>()?.petState
                updatedState?.let {
                    _petState.value = it
                    repository.saveToSharedPreferences(it)
                }
            }
        }
    }

    fun petStateSet(petState: PetState) {
        _petState.value = petState
        repository.saveToSharedPreferences(petState)
    }

    fun feedPet(pairId: String) {
        _petState.value = repository.feedPetSimple(_petState.value, pairId)
    }
}