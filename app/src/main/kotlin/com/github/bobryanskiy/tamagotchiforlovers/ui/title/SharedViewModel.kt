package com.github.bobryanskiy.tamagotchiforlovers.ui.title

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.data.PetConstants.PetConstants.HUNGER_RATE
import com.github.bobryanskiy.tamagotchiforlovers.data.notifications.Notifications
import com.github.bobryanskiy.tamagotchiforlovers.data.pairing.model.PairData
import com.github.bobryanskiy.tamagotchiforlovers.data.pet.model.PetState
import com.github.bobryanskiy.tamagotchiforlovers.data.title.SharedRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class SharedViewModel(application: Application, private val repository: SharedRepository) : AndroidViewModel(application) {
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
                    val t = (100 - (_petState.value!!.hunger)) / HUNGER_RATE
                    if (t != 0) {
                        Notifications.PetWantEat.cancel(application.applicationContext)
                        Notifications.PetWantEat.schedule(
                            application.applicationContext,
                            t
                        )
                    }
                }
            }
        }
    }

    fun petStateSet(petState: PetState) {
        _petState.value = petState
        repository.saveToSharedPreferences(petState)
    }

    fun feedPet(pairId: String, isOnline: Boolean) {
        viewModelScope.launch {
            _petState.value = repository.feedPetSimple(_petState.value, pairId, isOnline)
            Notifications.PetWantEat.cancel(application.applicationContext)
            Notifications.PetWantEat.schedule(application.applicationContext, (100 - (_petState.value!!.hunger)) / HUNGER_RATE)
        }
    }
}