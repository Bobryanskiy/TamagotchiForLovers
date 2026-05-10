package com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetTask
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.ApplyPetActionUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.GeneratePetTaskUseCase
import com.github.bobryanskiy.tamagotchiforlovers.ui.screen.PetActionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PuzzleUiState {
    data object Loading : PuzzleUiState
    data class Content(val task: PetTask) : PuzzleUiState
    data object Success : PuzzleUiState
    data class Error(val message: String, val task: PetTask) : PuzzleUiState
}

@HiltViewModel
class PuzzleViewModel @Inject constructor(
    private val generatePetTaskUseCase: GeneratePetTaskUseCase,
    private val applyPetActionUseCase: ApplyPetActionUseCase,
    private val petRepository: PetRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<PuzzleUiState>(PuzzleUiState.Loading)
    val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()

    private var currentTask: PetTask? = null
    private var currentPetId: String? = null
    private var currentAction: PetAction? = null

    init {
        val petId = savedStateHandle.get<String>("petId")
        val actionType = savedStateHandle.get<PetActionType>("actionType")

        if (petId != null && actionType != null) {
            currentPetId = petId
            currentAction = when (actionType) {
                PetActionType.Feed -> PetAction.Feed
                PetActionType.Rest -> PetAction.Rest
                PetActionType.Clean -> PetAction.Clean
                PetActionType.Play -> PetAction.Play
            }
            generateTask(petId)
        } else {
            _uiState.value = PuzzleUiState.Error("Invalid parameters", generatePetTaskUseCase())
        }
    }

    private fun generateTask(petId: String) {
        currentTask = generatePetTaskUseCase()
        _uiState.value = PuzzleUiState.Content(currentTask!!)
    }

    fun checkAnswer(answer: String) {
        val task = currentTask ?: return
        
        val isCorrect = answer.trim().equals(task.correctAnswer, ignoreCase = true)
        
        if (isCorrect) {
            _uiState.value = PuzzleUiState.Success
            currentPetId?.let { petId ->
                currentAction?.let { action ->
                    applyPetAction(petId, action)
                }
            }
        } else {
            _uiState.value = PuzzleUiState.Error("Неверный ответ! Попробуйте еще раз.", task)
        }
    }

    private fun applyPetAction(petId: String, action: PetAction) {
        viewModelScope.launch {
            applyPetActionUseCase(petId, action).fold(
                onSuccess = {
                    // Action applied successfully
                },
                onFailure = { error ->
                    // Handle error if needed
                }
            )
        }
    }
}
