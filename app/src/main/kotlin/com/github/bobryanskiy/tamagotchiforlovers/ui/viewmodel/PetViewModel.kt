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

    init {
        loadPet()
    }

    private fun loadPet() {
        viewModelScope.launch {
            // TODO: Get pet ID from navigation args or repository
            val petResult = petRepository.getPet("default_pet_id")
            petResult.fold(
                onSuccess = { pet ->
                    _uiState.value = PetUiState.Content(pet)
                },
                onFailure = { error ->
                    _uiState.value = PetUiState.Error("Failed to load pet: $error")
                }
            )
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

        viewModelScope.launch {
            // TODO: Show puzzle/example before applying action
            // For now, directly apply the action
            petRepository.applyAction(pet.id, action).fold(
                onSuccess = {
                    loadPet() // Reload pet stats
                },
                onFailure = { error ->
                    // TODO: Show error message
                }
            )
        }
    }

    fun onCreatePair() {
        // TODO: Navigate to create pair screen
    }

    fun onShowJoinRequests() {
        // TODO: Show join requests dialog/screen
    }
}
