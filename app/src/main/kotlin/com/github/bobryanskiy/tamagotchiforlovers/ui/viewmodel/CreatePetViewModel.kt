package com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.data.local.AppSessionStorage
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CreatePairUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CreatePetUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.ui.state.CreatePetUiState
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
 * ViewModel для экрана создания питомца (CreatePetScreen)
 * Отвечает ТОЛЬКО за создание питомца и/или пары
 */
@HiltViewModel
class CreatePetViewModel @Inject constructor(
    private val createPetUseCase: CreatePetUseCase,
    private val createPairUseCase: CreatePairUseCase,
    private val userRepository: UserRepository,
    private val sessionStorage: AppSessionStorage
) : ViewModel() {

    // 🟢 UI-состояние
    private val _uiState = MutableStateFlow(CreatePetUiState.idle())
    val uiState: StateFlow<CreatePetUiState> = _uiState.asStateFlow()

    // 🟡 Одноразовые события
    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    /** Создание только питомца (без пары) */
    fun createPetOnly(petName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isError = false)
            
            val ownerUserId = userRepository.getCurrentUserId()
            if (ownerUserId == null) {
                _uiState.value = CreatePetUiState.error("Пользователь не авторизован")
                _uiEvent.emit(UiEvent.ShowError("Пользователь не авторизован"))
                return@launch
            }

            when (val result = createPetUseCase(petName, ownerUserId)) {
                is DomainResult.Success -> {
                    val petId = result.data
                    sessionStorage.setActivePetId(petId)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                    _uiEvent.emit(UiEvent.NavigateToGame)
                }
                is DomainResult.Failure -> {
                    _uiState.value = CreatePetUiState.error("Не удалось создать питомца")
                    _uiEvent.emit(UiEvent.ShowError("Не удалось создать питомца"))
                }
            }
        }
    }

    /** Создание питомца и пары одновременно */
    fun createPetAndPair(petName: String, pairName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isError = false)
            
            val ownerUserId = userRepository.getCurrentUserId()
            if (ownerUserId == null) {
                _uiState.value = CreatePetUiState.error("Пользователь не авторизован")
                _uiEvent.emit(UiEvent.ShowError("Пользователь не авторизован"))
                return@launch
            }

            // 1. Создаём питомца
            val petResult = createPetUseCase(petName, ownerUserId)
            if (petResult is DomainResult.Failure) {
                _uiState.value = CreatePetUiState.error("Не удалось создать питомца")
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
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                    _uiEvent.emit(UiEvent.NavigateToLobby)
                }
                is DomainResult.Failure -> {
                    _uiState.value = CreatePetUiState.error("Ошибка создания пары")
                    _uiEvent.emit(UiEvent.ShowError("Ошибка создания пары"))
                }
            }
        }
    }

    /** Создание пары для существующего питомца */
    fun createPairForExistingPet(petId: String, pairName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isError = false)
            
            val ownerUserId = userRepository.getCurrentUserId()
            if (ownerUserId == null) {
                _uiState.value = CreatePetUiState.error("Пользователь не авторизован")
                _uiEvent.emit(UiEvent.ShowError("Пользователь не авторизован"))
                return@launch
            }

            when (val pairResult = createPairUseCase(ownerUserId, pairName, petId)) {
                is DomainResult.Success -> {
                    val newPairId = pairResult.data
                    sessionStorage.setActivePairId(newPairId)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                    _uiEvent.emit(UiEvent.NavigateToLobby)
                }
                is DomainResult.Failure -> {
                    _uiState.value = CreatePetUiState.error("Ошибка создания пары")
                    _uiEvent.emit(UiEvent.ShowError("Ошибка создания пары"))
                }
            }
        }
    }

    /** Обновление имени питомца в состоянии */
    fun updatePetName(name: String) {
        _uiState.value = _uiState.value.copy(petName = name)
    }

    /** Обновление названия пары в состоянии */
    fun updatePairName(name: String) {
        _uiState.value = _uiState.value.copy(pairName = name)
    }
}
