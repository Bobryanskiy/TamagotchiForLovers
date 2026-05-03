package com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.data.local.AppSessionStorage
import com.github.bobryanskiy.tamagotchiforlovers.data.notification.NotificationScheduler
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.ApplyPetActionUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CalculatePetAlertsUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.EvaluatePetCriticalStateUseCase
import com.github.bobryanskiy.tamagotchiforlovers.ui.state.GameUiState
import com.github.bobryanskiy.tamagotchiforlovers.ui.state.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана игры (GameScreen)
 * Отвечает ТОЛЬКО за логику игрового экрана
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val pairRepository: PairRepository,
    private val sessionStorage: AppSessionStorage,
    private val scheduler: NotificationScheduler,
    private val applyActionUseCase: ApplyPetActionUseCase,
    private val calcAlerts: CalculatePetAlertsUseCase,
    private val evalCritical: EvaluatePetCriticalStateUseCase
) : ViewModel() {

    // 🟢 UI-состояние (только данные для отображения)
    private val _uiState = MutableStateFlow(GameUiState.loading())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // 🟡 Одноразовые события (навигация, тосты)
    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private var currentPetId: String? = null
    private var petObservationJob: Job? = null

    init {
        Log.d("GameViewModel", "=== INIT STARTED ===")
        viewModelScope.launch {
            loadPetFromSession()
        }
    }

    /** Пытается загрузить питомца из сессии с повторами */
    private suspend fun loadPetFromSession(retries: Int = 5, delayMs: Long = 200) {
        repeat(retries) { attempt ->
            val firstId = sessionStorage.activePetId.first()
            Log.d("GameViewModel", "Attempt ${attempt + 1}/$retries: activePetId = $firstId")

            if (firstId != null) {
                loadPet(firstId)
                return
            } else {
                Log.d("GameViewModel", "activePetId is NULL - waiting ${delayMs}ms before retry")
                delay(delayMs)
            }
        }
        Log.w("GameViewModel", "Failed to load pet after $retries attempts")
        _uiEvent.emit(UiEvent.ShowError("Не удалось загрузить питомца"))
    }

    private fun loadPet(petId: String) {
        Log.d("GameViewModel", "loadPet STARTED for id: $petId")
        petObservationJob?.cancel()
        currentPetId = petId

        petObservationJob = viewModelScope.launch {
            petRepository.observePet(petId)
                .catch { e ->
                    Log.e("GameViewModel", "Failed to observe pet $petId", e)
                    _uiEvent.emit(UiEvent.ShowError("Ошибка загрузки питомца"))
                }
                .collect { pet ->
                    if (pet != null) {
                        _uiState.value = GameUiState.fromDomain(pet)
                        handlePetUpdate(pet)
                    } else {
                        _uiState.value = GameUiState.loading()
                    }
                }
        }
    }

    /** Обработка обновления питомца: критические состояния и будильники */
    private suspend fun handlePetUpdate(pet: com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet) {
        // Сохраняем активный ID
        if (sessionStorage.activePetId.first() != pet.id) {
            sessionStorage.setActivePetId(pet.id)
        }

        // Оцениваем критическое состояние
        val newState = evalCritical(pet.stats)

        // Если статус изменился — фиксируем в БД
        if (newState.status != pet.profile.criticalStatus) {
            petRepository.updateCriticalState(pet.id, newState)
        }

        // Маршрутизация по состояниям
        when (newState.status) {
            com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetCriticalStatus.DEAD,
            com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetCriticalStatus.ESCAPED -> {
                scheduler.cancelAlertsForPet(pet.id)
                val pairId = sessionStorage.activePairId.first()
                if (pairId != null) pairRepository.endSession(pairId)
                _uiEvent.emit(UiEvent.NavigateToGameOver)
                return
            }
            com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetCriticalStatus.COLLAPSED -> {
                scheduler.cancelAlertsForPet(pet.id)
                return
            }
            else -> {
                // NORMAL или SICK: пересчитываем будильники
                scheduler.cancelAlertsForPet(pet.id)
                val schedules = calcAlerts(pet)
                scheduler.scheduleAlerts(schedules)
            }
        }
    }

    /** Обработка действий игрока */
    fun onAction(action: PetAction) {
        val state = _uiState.value
        if (state.isActionsBlocked) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowError("Питомец сейчас не может это сделать"))
            }
            return
        }

        viewModelScope.launch {
            val petId = currentPetId ?: return@launch
            val pet = petRepository.getPet(petId) ?: return@launch

            when (val result = applyActionUseCase(pet, action)) {
                is DomainResult.Success -> {
                    Log.d("GameViewModel", "Action $action applied successfully")
                }
                is DomainResult.Failure -> {
                    val msg = when (result.error) {
                        is PetError.ActionNotAllowed -> "Действие недоступно"
                        is PetError.Network -> "Проблема с соединением"
                        else -> "Не удалось выполнить действие"
                    }
                    _uiEvent.emit(UiEvent.ShowError(msg))
                }
            }
        }
    }

    /** Принудительное завершение игры */
    fun abandonPet() {
        viewModelScope.launch {
            val petId = currentPetId ?: return@launch
            petRepository.abandonPet(petId)
            
            val pairId = sessionStorage.activePairId.first()
            if (pairId != null) pairRepository.endSession(pairId)
            
            sessionStorage.clearSession()
            scheduler.cancelAlertsForPet(petId)
            _uiEvent.emit(UiEvent.NavigateToHome)
        }
    }

    override fun onCleared() {
        super.onCleared()
        petObservationJob?.cancel()
    }
}
