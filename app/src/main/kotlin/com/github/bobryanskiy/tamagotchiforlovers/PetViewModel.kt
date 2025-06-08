package com.github.bobryanskiy.tamagotchiforlovers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.bobryanskiy.tamagotchiforlovers.data.login.LoginRepository

class PetViewModel(private val repository: PetRepository, private val logout: LoginRepository) : ViewModel() {
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

    fun logout() {
        logout.logout()
    }

    fun deletePet() {

    }
}