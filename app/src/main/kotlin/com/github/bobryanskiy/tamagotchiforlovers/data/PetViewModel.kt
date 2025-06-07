package com.github.bobryanskiy.tamagotchiforlovers.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.bobryanskiy.tamagotchiforlovers.PetState

class PetViewModel(private val repository: PetRepository) : ViewModel() {
    private val _petState = MutableLiveData<PetState>()
    val petState: LiveData<PetState> = _petState

    fun startObservingPetState(pairId: String) {
        repository.observePetState(pairId)
        repository.petState.observeForever { state ->
            _petState.postValue(state)
        }
    }

    fun feedPet(pairId: String) {
        val currentState = _petState.value ?: return
        val newPetState = currentState.copy(hunger = 100)
        repository.updatePetState(pairId, newPetState)
    }
}