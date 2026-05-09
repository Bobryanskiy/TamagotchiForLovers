package com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.DetermineEntryPointUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.DomainEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BootUiState {
    data object Loading : BootUiState
    data object NavigateToMain : BootUiState
    data class NavigateToPet(val petId: String) : BootUiState
    data object NavigateToAuth : BootUiState
}

@HiltViewModel
class BootViewModel @Inject constructor(
    private val determineEntryPointUseCase: DetermineEntryPointUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BootUiState>(BootUiState.Loading)
    val uiState: StateFlow<BootUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val entryPoint = determineEntryPointUseCase()
            _uiState.value = when (entryPoint) {
                is DomainEntryPoint.Auth -> BootUiState.NavigateToAuth
                is DomainEntryPoint.Main -> BootUiState.NavigateToMain
                is DomainEntryPoint.Pet -> BootUiState.NavigateToPet(entryPoint.petId)
            }
        }
    }
}
