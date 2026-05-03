package com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.data.local.AppSessionStorage
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.GenerateInviteKeyUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.RequestJoinUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.ui.state.ConnectionUiState
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
 * ViewModel для экрана подключения (ConnectionScreen)
 * Отвечает ТОЛЬКО за генерацию кода и подключение по коду
 */
@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val pairRepository: PairRepository,
    private val generateInviteKeyUseCase: GenerateInviteKeyUseCase,
    private val requestJoinUseCase: RequestJoinUseCase,
    private val sessionStorage: AppSessionStorage
) : ViewModel() {

    // 🟢 UI-состояние
    private val _uiState = MutableStateFlow(ConnectionUiState.idle())
    val uiState: StateFlow<ConnectionUiState> = _uiState.asStateFlow()

    // 🟡 Одноразовые события
    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private var currentPairId: String? = null

    init {
        loadActivePair()
    }

    /** Загрузка активной пары */
    private fun loadActivePair() {
        viewModelScope.launch {
            sessionStorage.activePairId.collect { pairId ->
                currentPairId = pairId
                if (pairId != null) {
                    val pair = pairRepository.getPair(pairId)
                    if (pair != null && pair.inviteKey != null) {
                        _uiState.value = _uiState.value.copy(
                            mode = ConnectionUiState.ConnectionMode.ONLINE_HOST,
                            inviteCode = pair.inviteKey
                        )
                    }
                }
            }
        }
    }

    /** Генерация кода приглашения */
    fun generateInviteCode() {
        val pairId = currentPairId
        
        if (pairId == null) {
            _uiEvent.emit(UiEvent.ShowError("Активная сессия не найдена"))
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGeneratingCode = true,
                isError = false
            )

            val pair = pairRepository.getPair(pairId)
            if (pair == null) {
                _uiState.value = _uiState.value.copy(
                    isGeneratingCode = false,
                    isError = true,
                    errorMessage = "Пара не найдена"
                )
                _uiEvent.emit(UiEvent.ShowError("Пара не найдена"))
                return@launch
            }

            when (val result = generateInviteKeyUseCase(pairId, pair)) {
                is DomainResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isGeneratingCode = false,
                        inviteCode = result.data,
                        mode = ConnectionUiState.ConnectionMode.ONLINE_HOST,
                        isSuccess = true
                    )
                    _uiEvent.emit(UiEvent.InviteCodeGenerated(result.data))
                    _uiEvent.emit(UiEvent.ShowMessage("Код создан: ${result.data}"))
                }
                is DomainResult.Failure -> {
                    val msg = when (result.error) {
                        is PairError.SessionNotActive -> "Сессия не активна"
                        is PairError.InvalidInput -> "Неверный ID пары"
                        else -> "Не удалось сгенерировать код"
                    }
                    _uiState.value = _uiState.value.copy(
                        isGeneratingCode = false,
                        isError = true,
                        errorMessage = msg
                    )
                    _uiEvent.emit(UiEvent.ShowError(msg))
                }
            }
        }
    }

    /** Подключение по коду приглашения */
    fun joinByCode(code: String) {
        if (code.isBlank()) {
            _uiEvent.emit(UiEvent.ShowError("Введите код приглашения"))
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isJoining = true,
                isError = false
            )

            when (val result = requestJoinUseCase(code.trim())) {
                is DomainResult.Success -> {
                    val pairId = result.data
                    sessionStorage.setActivePairId(pairId)
                    
                    _uiState.value = _uiState.value.copy(
                        isJoining = false,
                        mode = ConnectionUiState.ConnectionMode.ONLINE_GUEST,
                        isSuccess = true
                    )
                    _uiEvent.emit(UiEvent.ShowMessage("Успешное подключение!"))
                    _uiEvent.emit(UiEvent.NavigateToLobby)
                }
                is DomainResult.Failure -> {
                    val msg = when (result.error) {
                        is PairError.InvalidInput -> "Неверный код приглашения"
                        is PairError.SessionNotActive -> "Сессия не активна или уже заполнена"
                        else -> "Не удалось присоединиться"
                    }
                    _uiState.value = _uiState.value.copy(
                        isJoining = false,
                        isError = true,
                        errorMessage = msg
                    )
                    _uiEvent.emit(UiEvent.ShowError(msg))
                }
            }
        }
    }

    /** Обновление поля ввода кода */
    fun updateJoinCodeInput(code: String) {
        _uiState.value = _uiState.value.copy(joinCodeInput = code)
    }

    /** Выбор режима подключения */
    fun selectMode(mode: ConnectionUiState.ConnectionMode) {
        _uiState.value = _uiState.value.copy(mode = mode)
    }

    /** Сброс состояния */
    fun reset() {
        _uiState.value = ConnectionUiState.idle()
    }
}
