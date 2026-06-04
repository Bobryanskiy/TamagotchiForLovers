package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.DetermineEntryPointUseCase
import com.github.bobryanskiy.tamagotchiforlovers.presentation.navigation.AppRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BootViewModel @Inject constructor(
    private val determineEntryPointUseCase: DetermineEntryPointUseCase
) : ViewModel() {

    private val _navigationState = MutableStateFlow<AppRoute?>(null)
    val navigationState: StateFlow<AppRoute?> = _navigationState.asStateFlow()

    init {
        viewModelScope.launch {
            _navigationState.value = when (val entryPoint = determineEntryPointUseCase()) {
                is DetermineEntryPointUseCase.EntryPoint.Main -> AppRoute.Main
                is DetermineEntryPointUseCase.EntryPoint.Pet -> AppRoute.Pet(entryPoint.petId)
            }
        }
    }
}
