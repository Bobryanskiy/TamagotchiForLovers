package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CreatePairWithInviteUseCase
import com.github.bobryanskiy.tamagotchiforlovers.presentation.mapper.toUiErrorStringRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CreatePairUiState {
    data object Idle : CreatePairUiState
    data object Loading : CreatePairUiState
    data class Success(val pairId: String) : CreatePairUiState
    data class Error(@param:StringRes val messageResId: Int) : CreatePairUiState
}

@HiltViewModel
class CreatePairViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val createPairUseCase: CreatePairWithInviteUseCase
) : ViewModel() {

    private val petId: String = checkNotNull(savedStateHandle["petId"])

    private val _uiState = MutableStateFlow<CreatePairUiState>(CreatePairUiState.Idle)
    val uiState: StateFlow<CreatePairUiState> = _uiState.asStateFlow()

    private val _pairName = MutableStateFlow("")
    val pairName: StateFlow<String> = _pairName.asStateFlow()

    fun onNameChange(name: String) {
        _pairName.value = name
        if (_uiState.value is CreatePairUiState.Error) {
            _uiState.value = CreatePairUiState.Idle
        }
    }

    fun createPair() {
        val name = _pairName.value.trim()
        if (name.isBlank()) {
            _uiState.value = CreatePairUiState.Error(R.string.error_empty_pair_name)
            return
        }

        viewModelScope.launch {
            _uiState.value = CreatePairUiState.Loading
            _uiState.value = when (val result = createPairUseCase(name, petId)) {
                is DomainResult.Success -> CreatePairUiState.Success(result.data.pairId)
                is DomainResult.Failure -> CreatePairUiState.Error(result.error.toUiErrorStringRes())
            }
        }
    }
}