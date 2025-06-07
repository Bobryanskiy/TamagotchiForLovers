package com.github.bobryanskiy.tamagotchiforlovers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.bobryanskiy.tamagotchiforlovers.data.PairRepository

class PairViewModel(private val repository: PairRepository) : ViewModel() {
    private val _petState = MutableLiveData<PetState>()
    val petState: LiveData<PetState> = _petState

    fun createNewPair(callback: (String?) -> Unit) {
        repository.createNewPair(callback)
    }

    fun joinExistingPair(pairId: String, callback: () -> Unit) {
        repository.joinPair(pairId, callback)
    }

    fun observePetState(pairId: String) {
        repository.observePetState(pairId) { state ->
            _petState.postValue(state)
        }
    }

    fun feedPet(pairId: String) {
        repository.feedPet(pairId)
    }
}