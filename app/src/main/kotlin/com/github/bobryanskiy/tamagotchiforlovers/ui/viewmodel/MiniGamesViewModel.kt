package com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.ApplyPetActionUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.ui.state.MiniGamesUiState
import com.github.bobryanskiy.tamagotchiforlovers.ui.state.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана мини-игр (MiniGamesScreen)
 * Отвечает ТОЛЬКО за логику мини-игр для улучшения характеристик
 */
@HiltViewModel
class MiniGamesViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val applyPetActionUseCase: ApplyPetActionUseCase
) : ViewModel() {

    // 🟢 UI-состояние
    private val _uiState = MutableStateFlow(MiniGamesUiState.idle())
    val uiState: StateFlow<MiniGamesUiState> = _uiState.asStateFlow()

    // 🟡 Одноразовые события
    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    /** Выбор характеристики для улучшения */
    fun selectStat(statType: MiniGamesUiState.StatType) {
        _uiState.value = _uiState.value.copy(selectedStat = statType)
        
        // Автоматически выбираем игру для характеристики
        val gameType = when (statType) {
            MiniGamesUiState.StatType.HUNGER -> MiniGamesUiState.GameType.MATH_FEED
            MiniGamesUiState.StatType.HAPPINESS -> MiniGamesUiState.GameType.MEMORY_GAME
            MiniGamesUiState.StatType.ENERGY -> MiniGamesUiState.GameType.QUIZ_GAME
            MiniGamesUiState.StatType.HEALTH -> MiniGamesUiState.GameType.REFLEX_GAME
        }
        
        startGame(gameType)
    }

    /** Старт игры */
    fun startGame(gameType: MiniGamesUiState.GameType) {
        _uiState.value = _uiState.value.copy(
            currentGame = gameType,
            isPlaying = true,
            gameScore = 0
        )
    }

    /** Обновление счета в игре */
    fun updateScore(score: Int) {
        _uiState.value = _uiState.value.copy(gameScore = score)
    }

    /** Завершение игры с наградой */
    fun finishGame(reward: Int) {
        _uiState.value = _uiState.value.copy(
            isPlaying = false,
            rewardEarned = reward
        )
        
        _uiEvent.emit(UiEvent.ShowMessage("Получено +$reward к характеристике!"))
        
        // TODO: Применить награду к питомцу через UseCase
        // applyRewardToPet(reward)
    }

    /** Применение награды к питомцу */
    private fun applyRewardToPet(reward: Int) {
        viewModelScope.launch {
            val petId = petRepository.getActivePetId() ?: return@launch
            val pet = petRepository.getPet(petId) ?: return@launch
            
            // TODO: Создать UseCase для применения награды
            // val result = applyRewardUseCase(pet, reward, _uiState.value.selectedStat)
        }
    }

    /** Выход из мини-игры */
    fun exitGame() {
        _uiState.value = MiniGamesUiState.idle()
    }

    /** Повторная игра */
    fun replay() {
        val currentGame = _uiState.value.currentGame ?: return
        startGame(currentGame)
    }
}
