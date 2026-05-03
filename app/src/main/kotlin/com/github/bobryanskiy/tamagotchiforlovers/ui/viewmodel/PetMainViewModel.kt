package com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.data.local.AppSessionStorage
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.ApplyPetActionUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.ui.state.PetMainUiState
import com.github.bobryanskiy.tamagotchiforlovers.ui.state.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана питомца (PetMainScreen)
 * Отвечает ТОЛЬКО за отображение питомца и базовые действия
 */
@HiltViewModel
class PetMainViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val applyPetActionUseCase: ApplyPetActionUseCase,
    private val sessionStorage: AppSessionStorage
) : ViewModel() {

    // 🟢 UI-состояние
    private val _uiState = MutableStateFlow(PetMainUiState.loading())
    val uiState: StateFlow<PetMainUiState> = _uiState.asStateFlow()

    // 🟡 Одноразовые события
    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private var currentPetId: String? = null

    init {
        loadPet()
    }

    /** Загрузка питомца из сессии */
    fun loadPet() {
        viewModelScope.launch {
            _uiState.value = PetMainUiState.loading()
            
            val petId = sessionStorage.activePetId.first()
            if (petId == null) {
                _uiState.value = PetMainUiState.error("Питомец не найден")
                _uiEvent.emit(UiEvent.ShowError("Питомец не найден"))
                return@launch
            }

            currentPetId = petId
            
            petRepository.observePet(petId).collect { pet ->
                if (pet != null) {
                    _uiState.value = PetMainUiState.fromDomain(pet)
                } else {
                    _uiState.value = PetMainUiState.error("Питомец не найден")
                }
            }
        }
    }

    /** Кормление питомца (открытие мини-игры) */
    fun onFeedClick() {
        val state = _uiState.value
        if (state.isSleeping) {
            _uiEvent.emit(UiEvent.ShowMessage("Питомец спит! Разбудите его сначала."))
            return
        }
        
        generateMathProblem()
        _uiState.value = _uiState.value.copy(showFeedingGame = true)
    }

    /** Генерация математической задачи для кормления */
    private fun generateMathProblem() {
        val num1 = (1..10).random()
        val num2 = (1..10).random()
        val operation = listOf('+', '-', '*').random()
        
        val (question, answer) = when (operation) {
            '+' to "$num1 + $num2" to (num1 + num2)
            '-' to {
                if (num1 >= num2) {
                    "$num1 - $num2" to (num1 - num2)
                } else {
                    "$num2 - $num1" to (num2 - num1)
                }
            }
            '*' to "$num1 × $num2" to (num1 * num2)
            else to "$num1 + $num2" to (num1 + num2)
        }
        
        _uiState.value = _uiState.value.copy(
            feedingQuestion = "Реши: $question",
            feedingAnswer = answer
        )
    }

    /** Проверка ответа в мини-игре кормления */
    fun checkFeedingAnswer(userAnswer: Int) {
        val correctAnswer = _uiState.value.feedingAnswer
        
        if (userAnswer == correctAnswer) {
            performAction(PetAction.FEED)
            _uiEvent.emit(UiEvent.ShowMessage("Правильно! Питомец покормлен."))
        } else {
            _uiEvent.emit(UiEvent.ShowMessage("Неправильно! Попробуй ещё раз."))
        }
        
        _uiState.value = _uiState.value.copy(
            showFeedingGame = false,
            feedingQuestion = null,
            feedingAnswer = null
        )
    }

    /** Выполнение действия с питомцем */
    private fun performAction(action: PetAction) {
        val petId = currentPetId ?: return
        
        viewModelScope.launch {
            val pet = petRepository.getPet(petId) ?: return@launch
            
            when (val result = applyPetActionUseCase(pet, action)) {
                is DomainResult.Success -> {
                    // Успех обработается через observePet
                }
                is DomainResult.Failure -> {
                    _uiEvent.emit(UiEvent.ShowError("Не удалось выполнить действие"))
                }
            }
        }
    }

    /** Действие: играть */
    fun onPlayClick() {
        performAction(PetAction.PLAY)
    }

    /** Действие: спать */
    fun onSleepClick() {
        performAction(PetAction.SLEEP)
    }

    /** Действие: лечить */
    fun onHealClick() {
        performAction(PetAction.HEAL)
    }

    /** Закрытие мини-игры */
    fun dismissFeedingGame() {
        _uiState.value = _uiState.value.copy(
            showFeedingGame = false,
            feedingQuestion = null,
            feedingAnswer = null
        )
    }

    /** Переход к экрану мини-игр для улучшения характеристики */
    fun openMiniGames(statType: String) {
        // TODO: Навигация к экрану мини-игр
        _uiEvent.emit(UiEvent.ShowMessage("Мини-игры в разработке"))
    }
}
