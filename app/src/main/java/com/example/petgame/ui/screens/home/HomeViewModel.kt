package com.example.petgame.ui.screens.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * UI State для главного экрана
 */
data class HomeUiState(
    val hasActiveGame: Boolean = false,
    val activePetId: Int? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel для главного экрана.
 * Управляет состоянием и проверяет наличие активной игры.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    // private val getActivePetUseCase: GetActivePetUseCase // Будет добавлен позже
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        checkActiveGame()
    }

    /**
     * Проверяет наличие активной игры в локальной базе данных
     */
    private fun checkActiveGame() {
        // TODO: Вызвать UseCase для получения активного питомца из Room
        // viewModelScope.launch {
        //     _uiState.value = _uiState.value.copy(isLoading = true)
        //     try {
        //         val pet = getActivePetUseCase()
        //         _uiState.value = _uiState.value.copy(
        //             hasActiveGame = pet != null,
        //             activePetId = pet?.id,
        //             isLoading = false
        //         )
        //     } catch (e: Exception) {
        //         _uiState.value = _uiState.value.copy(
        //             error = e.message,
        //             isLoading = false
        //         )
        //     }
        // }
        
        // Заглушка для демонстрации
        _uiState.value = _uiState.value.copy(
            hasActiveGame = false,
            activePetId = null,
            isLoading = false
        )
    }

    fun onContinueGame(petId: Int) {
        // Логика продолжения игры будет обрабатываться в навигации
    }

    fun onErrorDisplayed() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
