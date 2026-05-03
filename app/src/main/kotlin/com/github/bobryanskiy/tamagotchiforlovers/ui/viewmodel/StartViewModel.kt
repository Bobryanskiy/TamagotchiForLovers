package com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.data.local.AppSessionStorage
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.ui.state.StartUiState
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
 * ViewModel для главного экрана (StartScreen)
 * Отвечает ТОЛЬКО за навигацию и проверку состояния сессии
 */
@HiltViewModel
class StartViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val pairRepository: PairRepository,
    private val userRepository: UserRepository,
    private val sessionStorage: AppSessionStorage
) : ViewModel() {

    // 🟢 UI-состояние
    private val _uiState = MutableStateFlow(StartUiState.loading())
    val uiState: StateFlow<StartUiState> = _uiState.asStateFlow()

    // 🟡 Одноразовые события
    private val _uiEvent = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    init {
        refreshState()
    }

    /** Обновление состояния при входе на экран */
    fun refreshState() {
        viewModelScope.launch {
            _uiState.value = StartUiState.loading()
            
            try {
                // Получаем активные ID из сессии
                val activePetId = sessionStorage.activePetId.first()
                val activePairId = sessionStorage.activePairId.first()
                val userName = userRepository.getCurrentUserName()

                var hasPet = false
                var hasPair = false
                var pairStatus: String? = null

                // Проверяем питомца
                if (activePetId != null) {
                    val pet = petRepository.getPet(activePetId)
                    hasPet = pet != null
                }

                // Проверяем пару
                if (activePairId != null) {
                    val pair = pairRepository.getPair(activePairId)
                    hasPair = pair != null
                    pairStatus = pair?.status?.name
                }

                _uiState.value = StartUiState.fromData(
                    hasPet = hasPet,
                    hasPair = hasPair,
                    pairStatus = pairStatus,
                    userName = userName
                )
            } catch (e: Exception) {
                Log.e("StartViewModel", "Failed to refresh state", e)
                _uiState.value = StartUiState.fromData(
                    hasPet = false,
                    hasPair = false
                )
            }
        }
    }

    /** Выход из аккаунта / сброс сессии */
    fun logout() {
        viewModelScope.launch {
            sessionStorage.clearSession()
            _uiState.value = StartUiState.fromData(
                hasPet = false,
                hasPair = false
            )
            _uiEvent.emit(UiEvent.ShowMessage("Сессия сброшена"))
        }
    }
}
