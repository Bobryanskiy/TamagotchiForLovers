package com.github.bobryanskiy.tamagotchiforlovers.ui.title

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.bobryanskiy.tamagotchiforlovers.data.pet.model.PetState
import com.github.bobryanskiy.tamagotchiforlovers.data.title.SharedRepository
import com.github.bobryanskiy.tamagotchiforlovers.util.Result

class SharedViewModel(private val repository: SharedRepository) : ViewModel() {
    private val _petState = MutableLiveData<PetState>()
    val petState: LiveData<PetState> = _petState

    fun subscribe(pairId: String) {
        val result = repository.subscribeToFirestore(pairId)
        if (result is Result.Success) {
            _petState.value = result.data.petState
        }
    }
}