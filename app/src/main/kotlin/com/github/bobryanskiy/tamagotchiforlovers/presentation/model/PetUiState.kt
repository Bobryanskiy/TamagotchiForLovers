package com.github.bobryanskiy.tamagotchiforlovers.presentation.model

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet

sealed class PetUiState {
    object Loading : PetUiState()
    data class Content(val pet: Pet) : PetUiState()
    data class Error(val error: PetError) : PetUiState()
}