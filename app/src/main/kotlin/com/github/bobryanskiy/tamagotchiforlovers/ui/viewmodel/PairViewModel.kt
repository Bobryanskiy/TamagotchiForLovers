package com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.data.local.AppSessionStorage
import com.github.bobryanskiy.tamagotchiforlovers.data.notification.NotificationScheduler
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.DomainError
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PairError
import com.github.bobryanskiy.tamagotchiforlovers.domain.error.PetError
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pair
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.onFailure
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.onSuccess
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.AcceptPlayerUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CreatePairUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CreatePetUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.EndSessionUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.GenerateInviteKeyUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.KickPlayerUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.LeaveSessionUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.RequestJoinUseCase
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
                                pair.currentPetId.let { _ ->
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
            when (val result = createPetUseCase(petName, ownerUserId)) {
                is DomainResult.Success -> {
                    val petId = result.data
                    sessionStorage.setActivePetId(petId)
                    _uiEvent.emit(UiEvent.NavigateToGame)
                }
                is DomainResult.Failure -> {
                    _uiEvent.emit(UiEvent.ShowError("Не удалось создать питомца"))
                }
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
            // 1. Создаём питомца через UseCase
            val petResult = createPetUseCase(petName, ownerUserId)
            if (petResult is DomainResult.Failure) {
                _uiEvent.emit(UiEvent.ShowError("Не удалось создать питомца"))
                return@launch
            }
            val petId = (petResult as DomainResult.Success).data

            // 2. Создаём пару через UseCase
            when (val pairResult = createPairUseCase(ownerUserId, pairName, petId)) {
                is DomainResult.Success -> {
                    val newPairId = pairResult.data
                    loadPair(newPairId)
                    _uiEvent.emit(UiEvent.NavigateToLobby)
                }
                is DomainResult.Failure -> {
                    _uiEvent.emit(UiEvent.ShowError("Ошибка создания пары"))
                }
            }
        }
    }

    // 🔑 Генерация кода приглашения через UseCase
    fun generateInviteCode() {
        val pairId = currentPairId ?: return
        val pair = _uiState.value ?: return
        viewModelScope.launch {
            when (val result = generateInviteKeyUseCase(pairId, pair)) {
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

    // 🤝 Принятие игрока (хост) через UseCase
    fun acceptRequest(guestId: String) {
        val pairId = currentPairId ?: return
        val pair = _uiState.value ?: return
        viewModelScope.launch {
            when (val result = acceptPlayerUseCase(pairId, pair, guestId)) {
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
                    // Успех обработается автоматически через collect (статус сменится на ACTIVE)
                }
            }
        }
    }

    // 🚪 Выход из сессии через UseCase
    fun leaveSession() {
        val pair = _uiState.value ?: return
        val pairId = currentPairId ?: return
        val currentUserId = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            when (val result = leaveSessionUseCase(pairId, pair, currentUserId)) {
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

    // 👢 Исключение второго игрока через UseCase
    fun kickPlayer() {
        val pair = _uiState.value ?: return
        val pairId = currentPairId ?: return
        val creatorId = userRepository.getCurrentUserId() ?: return
        val kickedUserId = pair.userId2 ?: return
        viewModelScope.launch {
            when (val result = kickPlayerUseCase(pairId, pair, kickedUserId, creatorId)) {
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

    // 🏁 Принудительное завершение игры через UseCase
    fun endGame() {
        val pair = _uiState.value ?: return
        val pairId = currentPairId ?: return
        val creatorId = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            when (val result = endSessionUseCase(pairId, pair, creatorId)) {
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

    // 🧹 Очистка состояния при закрытии пары
    private suspend fun handleSessionClosed() {
        sessionStorage.clearSession()
        // Отменяем алармы питомца, если он был привязан
        _uiState.value?.currentPetId?.let { notificationScheduler.cancelAlertsForPet(it) }
        _uiEvent.emit(UiEvent.NavigateToHome)
    }

    // 🔗 Создание пары для существующего питомца (без ввода имени питомца)
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
                    loadPair(newPairId)
                    _uiEvent.emit(UiEvent.NavigateToLobby)
                }
                is DomainResult.Failure -> {
                    _uiEvent.emit(UiEvent.ShowError("Ошибка создания пары"))
                }
            }
        }
    }

    // 🔄 Метод для принудительного обновления состояния пары
    fun refreshPairState() {
        val pairId = currentPairId ?: return
        loadPair(pairId)
    }

    fun joinSession(inviteCode: String) {
        viewModelScope.launch {
            requestJoinUseCase(inviteCode)
                .onSuccess {
                    _uiEvent.emit(UiEvent.NavigateToLobby)
                }
                .onFailure { error ->
                    // Маппинг происходит здесь, в UI слое
                    val errorMessageRes = error.mapToResource()
                    _uiEvent.emit(UiEvent.ShowErrorResource(errorMessageRes))
                }
        }
    }

    // Extension функция внутри ViewModel или отдельный файл в ui пакете
    private fun DomainError.mapToResource(): Int = when (this) {
        is PairError -> when (this) {
            is PairError.GuestOnly -> R.string.error_pair_guest_only
            is PairError.CreatorOnly -> R.string.error_pair_creator_only
            is PairError.SessionNotActive -> R.string.error_pair_session_not_active
            is PairError.AlreadyEnded -> R.string.error_pair_already_ended
            is PairError.AlreadyJoined -> R.string.error_pair_already_joined
            is PairError.InvalidInput -> R.string.error_pair_invalid_input
            is PairError.InvalidRequest -> R.string.error_pair_invalid_request
            is PairError.NotFound -> R.string.error_pair_not_found
            is PairError.Network -> R.string.error_pair_network
            is PairError.Unknown -> R.string.error_pair_unknown
        }
        is PetError -> when (this) {
            is PetError.InvalidInput -> R.string.error_pet_invalid_input
            is PetError.ActionNotAllowed -> R.string.error_pet_action_not_allowed
            is PetError.NotFound -> R.string.error_pet_not_found
            is PetError.Network -> R.string.error_pet_network
            is PetError.Unknown -> R.string.error_pet_unknown
        }
    }

    sealed class UiEvent {
        data class ShowError(val message: String) : UiEvent()
        data class ShowErrorResource(@param:StringRes val messageIdRes: Int) : UiEvent()
        data class InviteCodeGenerated(val code: String) : UiEvent()
        object NavigateToLobby : UiEvent()
        object NavigateToGame : UiEvent()
        object NavigateToHome : UiEvent()
    }
}