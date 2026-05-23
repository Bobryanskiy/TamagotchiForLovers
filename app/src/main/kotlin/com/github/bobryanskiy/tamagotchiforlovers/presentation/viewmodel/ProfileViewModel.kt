package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.onFailure
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Content(val pet: Pet, val ownerEmail: String?) : ProfileUiState()
    data class Error(@StringRes val messageResId: Int, val formatArg: String? = null) : ProfileUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(petId: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            if (petId.isBlank()) {
                _uiState.value = ProfileUiState.Error(R.string.profile_error_pet_not_selected)
                return@launch
            }

            petRepository.getPetById(petId).onSuccess { pet ->
                if (pet != null) {
                    val email = authRepository.getCurrentUserEmail()
                    _uiState.value = ProfileUiState.Content(pet, email)
                } else {
                    _uiState.value = ProfileUiState.Error(R.string.profile_error_pet_not_found)
                }
            }.onFailure { e ->
                _uiState.value = ProfileUiState.Error(R.string.profile_error_load_failed)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            if (authRepository.isLoggedIn()) {
                authRepository.signOut()
            }

            sessionRepository.clearAllSessionData()
        }
    }
}