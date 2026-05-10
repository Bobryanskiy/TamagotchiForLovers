package com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.ui.screen.PetActionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PetUiState {
    data object Loading : PetUiState
    data class Content(val pet: Pet) : PetUiState
    data class Error(val message: String) : PetUiState
}

@HiltViewModel
class PetViewModel @Inject constructor(
    private val petRepository: PetRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<PetUiState>(PetUiState.Loading)
    val uiState: StateFlow<PetUiState> = _uiState.asStateFlow()

    private var currentPetId: String? = null

    init {
        val petId = savedStateHandle.get<String>("petId")
        if (petId != null) {
            currentPetId = petId
            observePet(petId)
        } else {
            _uiState.value = PetUiState.Error("Pet ID not provided")
        }
    }

    private fun observePet(petId: String) {
        viewModelScope.launch {
            petRepository.observePet(petId).collect { pet ->
                when {
                    pet != null -> _uiState.value = PetUiState.Content(pet)
                    currentPetId != null -> _uiState.value = PetUiState.Error("Pet not found")
                }
            }
        }
    }

    fun onActionSelected(actionType: PetActionType) {
        val currentState = _uiState.value as? PetUiState.Content ?: return
        val pet = currentState.pet

        val action = when (actionType) {
            PetActionType.Feed -> PetAction.Feed
            PetActionType.Rest -> PetAction.Rest
            PetActionType.Clean -> PetAction.Clean
            PetActionType.Play -> PetAction.Play
        }
    }

    fun applyAction(petId: String, action: PetAction) {
        viewModelScope.launch {
            petRepository.applyAction(petId, action).fold(
                onSuccess = {
                },
                onFailure = { error ->
                    _uiState.value = PetUiState.Error("Failed to apply action: $error")
                }
            )
        }
    }
}
