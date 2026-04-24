package com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.data.local.AppSessionStorage
import com.github.bobryanskiy.tamagotchiforlovers.data.notification.NotificationScheduler
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetCriticalStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.ApplyPetActionUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CalculatePetAlertsUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.EvaluatePetCriticalStateUseCase
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

@HiltViewModel
class PetViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val pairRepository: PairRepository,
    private val sessionStorage: AppSessionStorage,
    private val scheduler: NotificationScheduler,
    private val applyActionUseCase: ApplyPetActionUseCase,
    private val calcAlerts: CalculatePetAlertsUseCase,
    private val evalCritical: EvaluatePetCriticalStateUseCase
) : ViewModel() {

    // 🟢 Состояние для UI
    private val _uiState = MutableStateFlow<Pet?>(null)
    val uiState: StateFlow<Pet?> = _uiState.asStateFlow()

    // 🟡 Одноразовые события (навигация, тосты)
    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()
    private var currentPetId: String? = null
    private var petObservationJob: Job? = null

    init {
        Log.d("DEBUG_PET", "=== INIT STARTED ===")
        viewModelScope.launch {
            loadPetFromSession()
        }
    }

    /** Пытается загрузить питомца из сессии с повторами */
    private suspend fun loadPetFromSession(retries: Int = 3, delayMs: Long = 100) {
        repeat(retries) { attempt ->
            val firstId = sessionStorage.activePetId.first()
            Log.d("DEBUG_PET", "Attempt ${attempt + 1}/$retries: activePetId.first() = $firstId")

            if (firstId != null) {
                Log.d("DEBUG_PET", "Calling loadPet($firstId)")
                loadPet(firstId)
                return
            } else {
                Log.d("DEBUG_PET", "activePetId is NULL - waiting ${delayMs}ms before retry")
                delay(delayMs)
            }
        }
        Log.w("DEBUG_PET", "Failed to load pet after $retries attempts")
    }

    private fun loadPet(petId: String) {
        Log.d("DEBUG_PET", "loadPet STARTED for id: $petId")
        // Отменяем предыдущее наблюдение, если было (защита от дублей)
        petObservationJob?.cancel()

        currentPetId = petId

        // Запускаем новое наблюдение и сохраняем джоб
        petObservationJob = viewModelScope.launch {
            Log.d("DEBUG_PET", "Starting observePet collection")
            petRepository.observePet(petId)
                .catch { e ->
                    Log.e("TAMAGOTCHI", "Failed to observe pet $petId", e)
                    _uiEvent.emit(UiEvent.ShowError("Ошибка загрузки"))
                }
                .collect { pet ->
                    Log.d("DEBUG_PET", "COLLECT received pet: ${pet?.profile?.name ?: "NULL"}")
                    _uiState.value = pet
                    if (pet != null) {
                        handlePetUpdate(pet)
                    }
                }
        }
    }

    // 🔥 Ядро логики: реагирует на каждое изменение статов из Firestore
    private suspend fun handlePetUpdate(pet: Pet) {
        // 1. Сохраняем активный ID в надёжное хранилище
        if (sessionStorage.activePetId.first() != pet.id) {
            sessionStorage.setActivePetId(pet.id)
        }

        // 2. Оцениваем критическое состояние (Смерть/Побег/Сон/Болезнь/Норма)
        val newState = evalCritical(pet.stats)

        // 3. Если статус изменился — фиксируем в БД (один раз)
        if (newState.status != pet.profile.criticalStatus) {
            petRepository.updateCriticalState(pet.id, newState)
        }

        // 4. Маршрутизация по состояниям
        when (newState.status) {
            PetCriticalStatus.DEAD, PetCriticalStatus.ESCAPED -> {
                scheduler.cancelAlertsForPet(pet.id)
                val pairId = sessionStorage.activePairId.first()
                if (pairId != null) pairRepository.endSession(pairId)
                _uiEvent.emit(UiEvent.NavigateToGameOver(newState.status))
                return
            }
            PetCriticalStatus.COLLAPSED -> {
                // Питомец спит: отменяем уведомления до восстановления
                scheduler.cancelAlertsForPet(pet.id)
                return
            }
            else -> {
                // NORMAL или SICK: пересчитываем будильники с учётом множителей
                scheduler.cancelAlertsForPet(pet.id)
                val schedules = calcAlerts(pet)
                scheduler.scheduleAlerts(schedules)
            }
        }
    }

    // 🎮 Обработка действий игрока (Покормить, Поиграть, Помыть, Уложить)
    fun onAction(action: PetAction) {
        val pet = _uiState.value ?: return

        // Быстрая блокировка на уровне UI (UseCase подстрахует на уровне Domain)
        if (pet.profile.criticalStatus in listOf(
                PetCriticalStatus.COLLAPSED,
                PetCriticalStatus.DEAD,
                PetCriticalStatus.ESCAPED
            )) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowError("Питомец сейчас не может это сделать"))
            }
            return
        }

        viewModelScope.launch {
            when (val result = applyActionUseCase(pet, action)) {
                is DomainResult.Success -> {
                    Log.d("TAMAGOTCHI", "Action $action applied successfully")
                    // Firestore обновит документ -> сработает collect -> handlePetUpdate пересчитает алармы
                }
                is DomainResult.Failure -> {
                    val msg = when (result.error) {
                        is PetError.ActionNotAllowed -> "Действие недоступно (питомец сыт/устал/не в настроении)"
                        is PetError.Network -> "Проблема с соединением. Попробуйте позже."
                        else -> "Не удалось выполнить действие"
                    }
                    _uiEvent.emit(UiEvent.ShowError(msg))
                }
            }
        }
    }

    // 🗑 Принудительное завершение игры пользователем
    fun abandonPet() {
        val pet = _uiState.value ?: return
        viewModelScope.launch {
            petRepository.abandonPet(pet.id)
            val pairId = sessionStorage.activePairId.first()
            if (pairId != null) pairRepository.endSession(pairId)
            sessionStorage.clearSession()
            scheduler.cancelAlertsForPet(pet.id)
            _uiEvent.emit(UiEvent.NavigateToHome)
        }
    }

    sealed class UiEvent {
        data class ShowError(val message: String) : UiEvent()
        data class NavigateToGameOver(val status: PetCriticalStatus) : UiEvent()
        object NavigateToHome : UiEvent()
    }
}