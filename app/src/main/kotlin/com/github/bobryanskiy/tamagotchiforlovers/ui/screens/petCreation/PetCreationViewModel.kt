package com.github.bobryanskiy.tamagotchiforlovers.ui.screens.petCreation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * UI State для экрана создания питомца
 */
data class PetCreationUiState(
    val petName: String = "",
    val selectedPetType: PetType = PetType.DOG,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val createdPetId: Int? = null
)

/**
 * ViewModel для экрана создания питомца.
 * Управляет процессом создания и сохранения питомца в локальной базе данных.
 */
@HiltViewModel
class PetCreationViewModel @Inject constructor(
    // private val createPetUseCase: CreatePetUseCase // Будет добавлен позже
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetCreationUiState())
    val uiState: StateFlow<PetCreationUiState> = _uiState.asStateFlow()

    /**
     * Обновляет имя питомца
     */
    fun updatePetName(name: String) {
        _uiState.value = _uiState.value.copy(
            petName = name,
            isError = false,
            errorMessage = null
        )
    }

    /**
     * Выбирает тип питомца
     */
    fun selectPetType(type: PetType) {
        _uiState.value = _uiState.value.copy(selectedPetType = type)
    }

    /**
     * Создает питомца и сохраняет в базу данных
     */
    fun createPet() {
        val currentState = _uiState.value
        
        if (currentState.petName.isBlank()) {
            _uiState.value = currentState.copy(
                isError = true,
                errorMessage = "Пожалуйста, введите имя питомца"
            )
            return
        }

        _uiState.value = currentState.copy(isLoading = true)

        // TODO: Вызвать UseCase для создания питомца в Room
        // viewModelScope.launch {
        //     try {
        //         val petId = createPetUseCase.execute(
        //             name = currentState.petName,
        //             type = currentState.selectedPetType
        //         )
        //         _uiState.value = _uiState.value.copy(
        //             isLoading = false,
        //             createdPetId = petId
        //         )
        //     } catch (e: Exception) {
        //         _uiState.value = _uiState.value.copy(
        //             isLoading = false,
        //             isError = true,
        //             errorMessage = e.message
        //         )
        //     }
        // }
    }

    /**
     * Сбрасывает состояние ошибки
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            isError = false,
            errorMessage = null
        )
    }

    /**
     * Сбрасывает ID созданного питомца после навигации
     */
    fun resetCreatedPetId() {
        _uiState.value = _uiState.value.copy(createdPetId = null)
    }
}
