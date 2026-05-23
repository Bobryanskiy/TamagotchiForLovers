package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.data.sync.PetSyncManager
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.onFailure
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.onSuccess
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.ApplyPetActionUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CalculateTimeDecayUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.MathTaskGeneratorUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import com.github.bobryanskiy.tamagotchiforlovers.presentation.mapper.toUiErrorStringRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PetUiState {
    object Loading : PetUiState()
    data class Content(val pet: Pet) : PetUiState()
    object GameOver : PetUiState()
}

sealed interface UiEvent {
    data class ShowError(val messageResId: Int) : UiEvent
    data class NavigateToRoute(val route: String) : UiEvent
}

data class TaskDialogState(
    val action: PetAction,
    val isProcessing: Boolean = false
)

@HiltViewModel
class PetViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val petRepository: PetRepository,
    private val sessionRepository: SessionRepository,
    private val petSyncManager: PetSyncManager,
    private val applyActionUseCase: ApplyPetActionUseCase,
    private val timeDecayUseCase: CalculateTimeDecayUseCase,
    val taskGenerator: MathTaskGeneratorUseCase,
    private val clock: Clock
) : ViewModel() {

    private val petId: String = checkNotNull(savedStateHandle["petId"]) { "petId missing" }

    private val _uiState = MutableStateFlow<PetUiState>(PetUiState.Loading)
    val uiState: StateFlow<PetUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private val _dialogState = MutableStateFlow<TaskDialogState?>(null)
    val dialogState: StateFlow<TaskDialogState?> = _dialogState.asStateFlow()

    init {
        observePet()
    }

    private fun observePet() {
        viewModelScope.launch {
            petRepository.observePet(petId)
                .map { pet ->
                    when {
                        pet == null -> PetUiState.Loading
                        pet.lifeState.isTerminal() -> PetUiState.GameOver
                        else -> {
                            val now = clock.currentTimeMillis()
                            val livePet = timeDecayUseCase(pet, now)
                            PetUiState.Content(livePet)
                        }
                    }
                }
                .collect { _uiState.value = it }
        }
    }

    fun onActionRequested(action: PetAction) {
        _dialogState.value = TaskDialogState(action = action, isProcessing = false)
    }

    fun onTaskCompleted() {
        val currentDialog = _dialogState.value ?: return
        viewModelScope.launch {
            _dialogState.value = currentDialog.copy(isProcessing = true)
            applyActionUseCase(petId, currentDialog.action)
                .onFailure { error ->
                    val resId = if (error is PetError) {
                        error.toUiErrorStringRes()
                    } else {
                        R.string.error_unknown
                    }
                    _uiEvent.emit(UiEvent.ShowError(resId))
                    _dialogState.value = null
                }
                .onSuccess {
                    launch { petSyncManager.syncPending() }
                    _dialogState.value = null
                }
        }
    }

    fun abandonPet(onDeleted: () -> Unit) {
        viewModelScope.launch {
            try {
                petRepository.deletePet(petId)
                sessionRepository.clearActivePetId()
                onDeleted()
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowError(R.string.error_unknown))
            }
        }
    }

    fun onDialogDismiss() {
        _dialogState.value = null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clearEvent() {
        _uiEvent.resetReplayCache()
    }
}