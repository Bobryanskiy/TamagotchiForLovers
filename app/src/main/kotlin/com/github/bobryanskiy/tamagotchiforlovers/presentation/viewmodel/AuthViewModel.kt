package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.UserResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.onFailure
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.onSuccess
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.LinkAccountUseCase
import com.github.bobryanskiy.tamagotchiforlovers.presentation.mapper.toUiErrorStringRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val messageResId: Int) : AuthUiState()
}

sealed interface AuthEvent {
    object NavigateToMain : AuthEvent
    data class ShowError(val messageResId: Int) : AuthEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val linkAccountUseCase: LinkAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val event: SharedFlow<AuthEvent> = _event.asSharedFlow()

    fun login(email: String, password: String) {
        executeAuthAction {
            authRepository.signIn(email, password)
        }
    }

    fun register(email: String, password: String) {
        executeAuthAction {
            authRepository.signUp(email, password)
        }
    }

    fun loginAsGuest() {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            _uiState.value = AuthUiState.Success
            _event.emit(AuthEvent.NavigateToMain)
        }
    }

    private fun executeAuthAction(authOperation: suspend () -> UserResult<Unit>) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authOperation()
                .onSuccess {
                    linkAccountUseCase.invoke()
                        .onFailure { linkError ->
                            // Ошибка линковки не должна блокировать вход, логируем или игнорируем
                            // Но состояние успеха всё равно устанавливаем
                        }

                    _uiState.value = AuthUiState.Success
                    _event.emit(AuthEvent.NavigateToMain)
                }
                .onFailure { error ->
                    val resId =  error.toUiErrorStringRes()
                    _uiState.value = AuthUiState.Error(resId)
                    _event.emit(AuthEvent.ShowError(resId))
                }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}