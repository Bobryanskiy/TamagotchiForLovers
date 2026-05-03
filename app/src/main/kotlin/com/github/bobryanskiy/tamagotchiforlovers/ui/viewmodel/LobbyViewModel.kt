package com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.data.local.AppSessionStorage
import com.github.bobryanskiy.tamagotchiforlovers.data.notification.NotificationScheduler
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.AcceptPlayerUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CreatePairUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CreatePetUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.EndSessionUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.GenerateInviteKeyUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.KickPlayerUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.LeaveSessionUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.RequestJoinUseCase
import com.github.bobryanskiy.tamagotchiforlovers.ui.state.LobbyUiState
import com.github.bobryanskiy.tamagotchiforlovers.ui.state.UiEvent
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

/**
 * ViewModel для экрана лобби (LobbyScreen)
 * Отвечает ТОЛЬКО за логику лобби: создание пары, приглашения, управление сессией
 */
@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val pairRepository: PairRepository,
    private val userRepository: UserRepository,
    private val sessionStorage: AppSessionStorage,
    private val notificationScheduler: NotificationScheduler,
    private val createPetUseCase: CreatePetUseCase,
    private val createPairUseCase: CreatePairUseCase,
    private val generateInviteKeyUseCase: GenerateInviteKeyUseCase,
    private val acceptPlayerUseCase: AcceptPlayerUseCase,
    private val kickPlayerUseCase: KickPlayerUseCase,
    private val leaveSessionUseCase: LeaveSessionUseCase,
    private val endSessionUseCase: EndSessionUseCase,
    private val requestJoinUseCase: RequestJoinUseCase
) : ViewModel() {

    // 🟢 UI-состояние
    private val _uiState = MutableStateFlow(LobbyUiState.loading())
    val uiState: StateFlow<LobbyUiState> = _uiState.asStateFlow()

    // 🟡 Одноразовые события
    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private var currentPairId: String? = null

    init {
        viewModelScope.launch {
            sessionStorage.activePairId.first()?.let { pairId -> loadPair(pairId) }
        }
    }

    private fun loadPair(pairId: String) {
        currentPairId = pairId
        viewModelScope.launch {
            pairRepository.observePair(pairId)
                .catch { e ->
                    Log.e("LobbyViewModel", "Failed to observe pair $pairId", e)
                    _uiEvent.emit(UiEvent.ShowError("Ошибка загрузки сессии"))
                }
                .collect { pair ->
                    if (pair != null) {
                        val currentUserId = userRepository.getCurrentUserId()
                        _uiState.value = LobbyUiState.fromDomain(pair, currentUserId)
                        
                        // Сохраняем ID пары и питомца
                        sessionStorage.setActivePairId(pair.id)
                        pair.currentPetId?.let { petId ->
                            sessionStorage.setActivePetId(petId)
                        }

                        // Авто-реакция на статус
                        when (pair.status) {
                            PairStatus.ACTIVE -> {
                                _uiEvent.emit(UiEvent.NavigateToGame)
                            }
                            PairStatus.PENDING -> {
                                if (pair.userId1 == currentUserId && pair.inviteKey == null) {
                                    generateInviteCode()
                                }
                            }
                            PairStatus.ENDED -> {
                                handleSessionClosed()
                            }
                        }
                    } else {
                        _uiState.value = LobbyUiState.loading()
                    }
                }
        }
    }

    /** Создание питомца и пары (для нового пользователя) */
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
            val petResult = createPetUseCase(petName, ownerUserId)
            if (petResult is DomainResult.Failure) {
                _uiEvent.emit(UiEvent.ShowError("Не удалось создать питомца"))
                return@launch
            }
            val petId = (petResult as DomainResult.Success).data

            // 2. Создаём пару
            when (val pairResult = createPairUseCase(ownerUserId, pairName, petId)) {
                is DomainResult.Success -> {
                    val newPairId = pairResult.data
                    sessionStorage.setActivePairId(newPairId)
                    sessionStorage.setActivePetId(petId)
                    loadPair(newPairId)
                    _uiEvent.emit(UiEvent.NavigateToLobby)
                }
                is DomainResult.Failure -> {
                    _uiEvent.emit(UiEvent.ShowError("Ошибка создания пары"))
                }
            }
        }
    }

    /** Создание пары для существующего питомца */
    fun createPairForExistingPet(petId: String, pairName: String) {
        val ownerUserId = userRepository.getCurrentUserId()
        if (ownerUserId == null) {
            viewModelScope.launch {
                _uiEvent.emit(UiEvent.ShowError("Пользователь не авторизован"))
            }
            return
        }

        viewModelScope.launch {
            when (val pairResult = createPairUseCase(ownerUserId, pairName, petId)) {
                is DomainResult.Success -> {
                    val newPairId = pairResult.data
                    sessionStorage.setActivePairId(newPairId)
                    sessionStorage.setActivePetId(petId)
                    loadPair(newPairId)
                }
                is DomainResult.Failure -> {
                    _uiEvent.emit(UiEvent.ShowError("Ошибка создания пары"))
                }
            }
        }
    }

    /** Генерация кода приглашения */
    fun generateInviteCode() {
        val pairId = currentPairId ?: return
        val pair = _uiState.value.let { state ->
            // Восстанавливаем Pair из состояния невозможно, нужен отдельный метод
            // Поэтому просто вызываем use case с ID
            null
        }

        viewModelScope.launch {
            // Получаем актуальное состояние пары
            val currentPair = pairRepository.getPair(pairId)
            if (currentPair == null) {
                _uiEvent.emit(UiEvent.ShowError("Пара не найдена"))
                return@launch
            }

            when (val result = generateInviteKeyUseCase(pairId, currentPair)) {
                is DomainResult.Success -> {
                    _uiEvent.emit(UiEvent.InviteCodeGenerated(result.data))
                }
                is DomainResult.Failure -> {
                    val msg = when (result.error) {
                        is PairError.SessionNotActive -> "Сессия не активна"
                        is PairError.InvalidInput -> "Неверный ID пары"
                        else -> "Не удалось сгенерировать код"
                    }
                    _uiEvent.emit(UiEvent.ShowError(msg))
                }
            }
        }
    }

    /** Принятие игрока (хост) */
    fun acceptRequest(guestId: String) {
        val pairId = currentPairId ?: return
        viewModelScope.launch {
            val currentPair = pairRepository.getPair(pairId) ?: return@launch

            when (val result = acceptPlayerUseCase(pairId, currentPair, guestId)) {
                is DomainResult.Failure -> {
                    val msg = when (result.error) {
                        is PairError.SessionNotActive -> "Сессия не активна"
                        is PairError.InvalidRequest -> "Неверный запрос"
                        is PairError.InvalidInput -> "Неверные данные"
                        else -> "Не удалось принять игрока"
                    }
                    _uiEvent.emit(UiEvent.ShowError(msg))
                }
                is DomainResult.Success -> {
                    // Успех обработается через observePair
                }
            }
        }
    }

    /** Выход из сессии */
    fun leaveSession() {
        val pairId = currentPairId ?: return
        val currentUserId = userRepository.getCurrentUserId() ?: return
        
        viewModelScope.launch {
            val currentPair = pairRepository.getPair(pairId) ?: return@launch

            when (val result = leaveSessionUseCase(pairId, currentPair, currentUserId)) {
                is DomainResult.Success -> {
                    handleSessionClosed()
                }
                is DomainResult.Failure -> {
                    val msg = when (result.error) {
                        is PairError.SessionNotActive -> "Сессия не активна"
                        is PairError.GuestOnly -> "Только гость может выйти"
                        is PairError.InvalidInput -> "Неверные данные"
                        else -> "Не удалось покинуть сессию"
                    }
                    _uiEvent.emit(UiEvent.ShowError(msg))
                }
            }
        }
    }

    /** Исключение второго игрока (хост) */
    fun kickPlayer() {
        val pairId = currentPairId ?: return
        val creatorId = userRepository.getCurrentUserId() ?: return
        val state = _uiState.value
        val kickedUserId = state.pendingGuestId ?: return

        viewModelScope.launch {
            val currentPair = pairRepository.getPair(pairId) ?: return@launch

            when (val result = kickPlayerUseCase(pairId, currentPair, kickedUserId, creatorId)) {
                is DomainResult.Failure -> {
                    val msg = when (result.error) {
                        is PairError.SessionNotActive -> "Сессия не активна"
                        is PairError.CreatorOnly -> "Только создатель может исключить"
                        is PairError.InvalidRequest -> "Неверный запрос"
                        is PairError.InvalidInput -> "Неверные данные"
                        else -> "Не удалось исключить игрока"
                    }
                    _uiEvent.emit(UiEvent.ShowError(msg))
                }
                is DomainResult.Success -> {
                    // Успех обработается через observePair
                }
            }
        }
    }

    /** Завершение игры (хост) */
    fun endGame() {
        val pairId = currentPairId ?: return
        val creatorId = userRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            val currentPair = pairRepository.getPair(pairId) ?: return@launch

            when (val result = endSessionUseCase(pairId, currentPair, creatorId)) {
                is DomainResult.Success -> {
                    handleSessionClosed()
                }
                is DomainResult.Failure -> {
                    val msg = when (result.error) {
                        is PairError.CreatorOnly -> "Только создатель может завершить"
                        is PairError.AlreadyEnded -> "Сессия уже завершена"
                        is PairError.SessionNotActive -> "Сессия не активна"
                        is PairError.InvalidInput -> "Неверные данные"
                        else -> "Не удалось завершить игру"
                    }
                    _uiEvent.emit(UiEvent.ShowError(msg))
                }
            }
        }
    }

    /** Присоединение к сессии по коду */
    fun joinSession(inviteCode: String) {
        viewModelScope.launch {
            when (val result = requestJoinUseCase(inviteCode)) {
                is DomainResult.Success -> {
                    val pairId = result.data
                    sessionStorage.setActivePairId(pairId)
                    loadPair(pairId)
                }
                is DomainResult.Failure -> {
                    val msg = when (result.error) {
                        is PairError.InvalidInput -> "Неверный код приглашения"
                        is PairError.SessionNotActive -> "Сессия не активна"
                        else -> "Не удалось присоединиться"
                    }
                    _uiEvent.emit(UiEvent.ShowError(msg))
                }
            }
        }
    }

    /** Очистка состояния при закрытии пары */
    private suspend fun handleSessionClosed() {
        sessionStorage.clearSession()
        _uiState.value.let { state ->
            // Отменяем алармы (если были)
        }
        _uiEvent.emit(UiEvent.NavigateToHome)
    }
}
