package com.example.petgame.ui.screens.petMain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * UI State для основного экрана с питомцем
 */
data class PetMainUiState(
    val petId: Int? = null,
    val petName: String = "",
    val petLevel: Int = 1,
    val petType: String = "",
    val stats: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showFeedingDialog: Boolean = false,
    val feedingTask: FeedingTask? = null
)

/**
 * Задача для кормления (математическая задачка)
 */
data class FeedingTask(
    val question: String,
    val correctAnswer: Int,
    val options: List<Int>
)

/**
 * ViewModel для основного экрана с питомцем.
 * Управляет состоянием питомца, кормлением и характеристиками.
 */
@HiltViewModel
class PetMainViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    // private val getPetByIdUseCase: GetPetByIdUseCase,
    // private val feedPetUseCase: FeedPetUseCase,
    // private val updateStatUseCase: UpdateStatUseCase
) : ViewModel() {

    private val petId: Int = savedStateHandle.get<Int>("petId") ?: throw IllegalArgumentException("petId required")

    private val _uiState = MutableStateFlow(PetMainUiState(petId = petId))
    val uiState: StateFlow<PetMainUiState> = _uiState.asStateFlow()

    init {
        loadPetData()
    }

    /**
     * Загружает данные питомца из базы данных
     */
    private fun loadPetData() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        // TODO: Вызвать UseCase для получения данных питомца
        // viewModelScope.launch {
        //     try {
        //         val pet = getPetByIdUseCase.execute(petId)
        //         _uiState.value = _uiState.value.copy(
        //             petName = pet.name,
        //             petLevel = pet.level,
        //             petType = pet.type.name,
        //             stats = mapOf(
        //                 "STRENGTH" to pet.strength,
        //                 "AGILITY" to pet.agility,
        //                 "INTELLIGENCE" to pet.intelligence,
        //                 "HEALTH" to pet.health
        //             ),
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
            petName = "Барбос",
            petLevel = 5,
            petType = "DOG",
            stats = mapOf(
                "STRENGTH" to 10,
                "AGILITY" to 8,
                "INTELLIGENCE" to 12,
                "HEALTH" to 15
            ),
            isLoading = false
        )
    }

    /**
     * Показывает диалог кормления с математической задачей
     */
    fun showFeedingDialog() {
        val task = generateFeedingTask()
        _uiState.value = _uiState.value.copy(
            showFeedingDialog = true,
            feedingTask = task
        )
    }

    /**
     * Скрывает диалог кормления
     */
    fun hideFeedingDialog() {
        _uiState.value = _uiState.value.copy(
            showFeedingDialog = false,
            feedingTask = null
        )
    }

    /**
     * Проверяет ответ на задачу кормления
     */
    fun checkFeedingAnswer(answer: Int) {
        val task = _uiState.value.feedingTask ?: return
        
        if (answer == task.correctAnswer) {
            // TODO: Вызвать UseCase для кормления питомца
            // viewModelScope.launch {
            //     feedPetUseCase.execute(petId)
            // }
        }
        
        hideFeedingDialog()
    }

    /**
     * Генерирует математическую задачу для кормления
     */
    private fun generateFeedingTask(): FeedingTask {
        val num1 = (1..10).random()
        val num2 = (1..10).random()
        val correctAnswer = num1 + num2
        
        val options = buildList {
            add(correctAnswer)
            while (size < 4) {
                val wrongAnswer = correctAnswer + (-3..3).random().takeIf { it != 0 } ?: 0
                if (!contains(wrongAnswer) && wrongAnswer > 0) {
                    add(wrongAnswer)
                }
            }.shuffled()
        }

        return FeedingTask(
            question = "$num1 + $num2 = ?",
            correctAnswer = correctAnswer,
            options = options
        )
    }

    /**
     * Запрашивает переход к мини-игре для улучшения характеристики
     */
    fun navigateToMiniGames(statType: String) {
        // Навигация обрабатывается в экране
    }

    fun onErrorDisplayed() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
