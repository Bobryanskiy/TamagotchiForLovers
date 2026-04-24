package com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.data.local.AppSessionStorage
import com.github.bobryanskiy.tamagotchiforlovers.data.notification.NotificationScheduler
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pair
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
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
class PairViewModel @Inject constructor(
    private val pairRepository: PairRepository,
    private val petRepository: PetRepository,
    private val userRepository: UserRepository,
    private val sessionStorage: AppSessionStorage,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    // 🟢 Состояние для UI (Пара или null)
    private val _uiState = MutableStateFlow<Pair?>(null)
    val uiState: StateFlow<Pair?> = _uiState.asStateFlow()

    // 🟡 Одноразовые события (навигация, тосты, коды)
    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private var currentPairId: String? = null

    init {
        // 🚀 Восстанавливаем сессию при запуске
        viewModelScope.launch {
            sessionStorage.activePairId
                .first()
                ?.let { pairId -> loadPair(pairId) }
        }
    }

    private fun loadPair(pairId: String) {
        currentPairId = pairId
        viewModelScope.launch {
            pairRepository.observePair(pairId)
                .catch { e ->
                    Log.e("TAMAGOTCHI", "Failed to observe pair $pairId", e)
                    _uiEvent.emit(UiEvent.ShowError("Ошибка загрузки сессии"))
                }
                .collect { pair ->
                    _uiState.value = pair
                    if (pair != null) {
                        // Сохраняем активную пару
                        sessionStorage.setActivePairId(pair.id)

                        // Авто-реакция на изменение статуса
                        when (pair.status) {
                            PairStatus.ACTIVE -> {
                                // Пара активна → синхронизируем petId для PetViewModel
                                pair.currentPetId.let { petId ->
                                    _uiEvent.emit(UiEvent.NavigateToGame)
                                }
                            }
                            PairStatus.PENDING -> {
                                if (pair.userId1 == userRepository.getCurrentUserId() && pair.inviteKey == null)
                                    generateInviteCode()
                            }
                            PairStatus.ENDED -> {
                                // Игра завершена → чистим сессию и ведём на главную
                                handleSessionClosed()
                            }
                            else -> {} // PENDING → остаёмся в лобби
                        }
                    }
                }
        }
    }

    fun createPet(petName: String) {
        val ownerUserId = userRepository.getCurrentUserId()
        if (ownerUserId == null) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowError("Пользователь не авторизован"))
            }
            return
        }

        viewModelScope.launch {
            val petResult = petRepository.createPet(petName, ownerUserId)
            if (petResult is DomainResult.Success) {
                val petId = petResult.data

                // Сохраняем в сессию
                sessionStorage.setActivePetId(petId)

                // Идём на экран игры
                _uiEvent.emit(UiEvent.NavigateToGame)
            } else {
                _uiEvent.emit(UiEvent.ShowError("Не удалось создать питомца"))
            }
        }
    }

    // 🆕 Создание питомца и пары
    fun createNewSession(petName: String, pairName: String) {
        val ownerUserId = userRepository.getCurrentUserId()
        if (ownerUserId == null) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowError("Пользователь не авторизован"))
            }
            return
        }
        viewModelScope.launch {
            // 1. Создаём питомца
            val petResult = petRepository.createPet(petName, ownerUserId)
            if (petResult is DomainResult.Failure) {
                _uiEvent.emit(UiEvent.ShowError("Не удалось создать питомца"))
                return@launch
            }
            val petId = (petResult as DomainResult.Success).data

            // 2. Создаём пару
            val pairResult = pairRepository.createPair(ownerUserId, pairName, petId)
            if (pairResult is DomainResult.Success) {
                val newPairId = pairResult.data
                loadPair(newPairId)
                _uiEvent.emit(UiEvent.NavigateToLobby)
            } else {
                _uiEvent.emit(UiEvent.ShowError("Ошибка создания пары"))
            }
        }
    }

    // 🔑 Генерация кода приглашения
    fun generateInviteCode() {
        val pairId = currentPairId ?: return
        viewModelScope.launch {
            val result = pairRepository.generateInviteKey(pairId)
            if (result is DomainResult.Success) {
                _uiEvent.emit(UiEvent.InviteCodeGenerated(result.data))
            } else {
                _uiEvent.emit(UiEvent.ShowError("Не удалось сгенерировать код"))
            }
        }
    }

    // 🤝 Принятие игрока (хост)
    fun acceptRequest(guestId: String) {
        val pairId = currentPairId ?: return
        viewModelScope.launch {
            val result = pairRepository.acceptPlayer(pairId, guestId)
            if (result is DomainResult.Failure) {
                _uiEvent.emit(UiEvent.ShowError("Не удалось принять игрока"))
            }
            // Успех обработается автоматически через collect (статус сменится на ACTIVE)
        }
    }

    // 🚪 Выход из сессии
    fun leaveSession() {
        val pair = _uiState.value ?: return
        viewModelScope.launch {
            // В реальном проекте userId берётся из AuthRepository.currentUser.uid
            val currentUserId = pair.userId1 ?: return@launch
            val result = pairRepository.leaveSession(pair.id, currentUserId)
            if (result is DomainResult.Success) {
                handleSessionClosed()
            } else {
                _uiEvent.emit(UiEvent.ShowError("Не удалось покинуть сессию"))
            }
        }
    }

    // 👢 Исключение второго игрока
    fun kickPlayer() {
        val pair = _uiState.value ?: return
        viewModelScope.launch {
            val result = pairRepository.kickPlayer(pair.id, pair.userId2 ?: "")
            if (result is DomainResult.Failure) {
                _uiEvent.emit(UiEvent.ShowError("Не удалось исключить игрока"))
            }
        }
    }

    // 🏁 Принудительное завершение игры
    fun endGame() {
        val pairId = currentPairId ?: return
        viewModelScope.launch {
            val result = pairRepository.endSession(pairId)
            if (result is DomainResult.Success) {
                handleSessionClosed()
            }
        }
    }

    // 🧹 Очистка состояния при закрытии пары
    private suspend fun handleSessionClosed() {
        sessionStorage.clearSession()
        // Отменяем алармы питомца, если он был привязан
        _uiState.value?.currentPetId?.let { notificationScheduler.cancelAlertsForPet(it) }
        _uiEvent.emit(UiEvent.NavigateToHome)
    }

    sealed class UiEvent {
        data class ShowError(val message: String) : UiEvent()
        data class InviteCodeGenerated(val code: String) : UiEvent()
        object NavigateToLobby : UiEvent()
        object NavigateToGame : UiEvent()
        object NavigateToHome : UiEvent()
    }
}