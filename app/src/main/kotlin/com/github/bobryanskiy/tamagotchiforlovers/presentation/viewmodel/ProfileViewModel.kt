package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Content(
        val email: String?,
        val isGuest: Boolean,
        val activePet: Pet?
    ) : ProfileUiState()
    data class Error(@param:StringRes val messageResId: Int) : ProfileUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            val email = authRepository.getCurrentUserEmail()
            val isGuest = !authRepository.isLoggedIn()
            val petId = sessionRepository.getActivePetId()

            val pet = petId?.let {
                when (val result = petRepository.getPetById(it)) {
                    is DomainResult.Success -> result.data
                    is DomainResult.Failure -> null
                }
            }

            _uiState.value = ProfileUiState.Content(
                email = email,
                isGuest = isGuest,
                activePet = pet
            )
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
