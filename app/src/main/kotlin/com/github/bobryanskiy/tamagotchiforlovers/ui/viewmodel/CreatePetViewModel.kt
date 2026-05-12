package com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CreatePetUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CreatePetUiState {
    data object Idle : CreatePetUiState
    data object Loading : CreatePetUiState
    data class Success(val petId: String) : CreatePetUiState
    data class Error(val message: String) : CreatePetUiState
}

@HiltViewModel
class CreatePetViewModel @Inject constructor(
    private val createPetUseCase: CreatePetUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreatePetUiState>(CreatePetUiState.Idle)
    val uiState: StateFlow<CreatePetUiState> = _uiState.asStateFlow()

    private val _petName = MutableStateFlow("")
    val petName: String get() = _petName.value

    fun onNameChange(name: String) {
        _petName.value = name
        if (_uiState.value is CreatePetUiState.Error) {
            _uiState.value = CreatePetUiState.Idle
        }
    }

    fun createPet() {
        val name = _petName.value.trim()
        val ownerUserId = auth.currentUser?.uid ?: run {
            _uiState.value = CreatePetUiState.Error("User not authorized")
            return
        }

        if (name.isBlank()) {
            _uiState.value = CreatePetUiState.Error("Pet name cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = CreatePetUiState.Loading
            val result = createPetUseCase(name, ownerUserId)
            _uiState.value = when (result) {
                is DomainResult.Success -> CreatePetUiState.Success(result.data)
                is DomainResult.Failure -> CreatePetUiState.Error("Failed to create pet: ${result.error}")
            }
        }
    }
}