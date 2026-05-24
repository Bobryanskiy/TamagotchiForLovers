package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.util.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RenamePetUiState {
    data object Loading : RenamePetUiState
    data class Loaded(val currentName: String) : RenamePetUiState
    data object Success : RenamePetUiState
    data class Error(@param:StringRes val messageResId: Int) : RenamePetUiState
}

@HiltViewModel
class RenamePetViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val petRepository: PetRepository
) : ViewModel() {

    private val petId: String = checkNotNull(savedStateHandle["petId"])

    private val _uiState = MutableStateFlow<RenamePetUiState>(RenamePetUiState.Loading)
    val uiState: StateFlow<RenamePetUiState> = _uiState.asStateFlow()

    private val _newName = MutableStateFlow("")
    val newName: StateFlow<String> = _newName.asStateFlow()

    init {
        loadCurrentName()
    }

    private fun loadCurrentName() {
        viewModelScope.launch {
            when (val result = petRepository.getPetById(petId)) {
                is DomainResult.Success -> {
                    val pet = result.data
                    if (pet != null) {
                        _newName.value = pet.profile.name
                        _uiState.value = RenamePetUiState.Loaded(pet.profile.name)
                    } else {
                        _uiState.value = RenamePetUiState.Error(R.string.error_pet_not_found)
                    }
                }
                is DomainResult.Failure -> _uiState.value = RenamePetUiState.Error(R.string.error_unknown)
            }
        }
    }

    fun onNameChange(name: String) {
        _newName.value = name
        if (_uiState.value is RenamePetUiState.Error) {
            val currentError = (_uiState.value as RenamePetUiState.Error).messageResId
            // Сбрасываем только если это ошибка валидации, не загрузки
            if (currentError in listOf(
                    R.string.error_empty_pet_name,
                    R.string.error_pet_name_too_short,
                    R.string.error_pet_name_too_long,
                    R.string.error_pet_name_invalid_chars
                )) {
                _uiState.value = RenamePetUiState.Loaded(_newName.value)
            }
        }
    }

    fun rename() {
        val name = _newName.value.trim()

        val errorResId = ValidationUtils.getPetNameErrorResId(name)
        if (errorResId != null) {
            _uiState.value = RenamePetUiState.Error(errorResId)
            return
        }

        viewModelScope.launch {
            when (val result = petRepository.updatePetName(petId, name)) {
                is DomainResult.Success -> _uiState.value = RenamePetUiState.Success
                is DomainResult.Failure -> _uiState.value = RenamePetUiState.Error(R.string.error_unknown)
            }
        }
    }
}