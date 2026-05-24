package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.UserResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.LinkAccountUseCase
import com.github.bobryanskiy.tamagotchiforlovers.presentation.mapper.toUiErrorStringRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data object Success : AuthUiState()
    data class Error(val messageResId: Int) : AuthUiState()
}

sealed interface AuthEvent {
    data object NavigateToMain : AuthEvent
    data class NavigateToPet(val petId: String) : AuthEvent
    data class ShowError(val messageResId: Int) : AuthEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val linkAccountUseCase: LinkAccountUseCase,
    private val sessionRepository: SessionRepository,
    private val logger: AppLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val event: SharedFlow<AuthEvent> = _event.asSharedFlow()

    companion object {
        private const val TAG = "AuthVM"
    }

    fun login(email: String, password: String) {
        executeAuthAction { authRepository.signIn(email, password) }
    }

    fun register(email: String, password: String) {
        executeAuthAction { authRepository.signUp(email, password) }
    }

    private fun executeAuthAction(authOperation: suspend () -> UserResult<Unit>) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            when (val result = authOperation()) {
                is DomainResult.Success -> {
                    // Привязываем аккаунт
                    when (val linkResult = linkAccountUseCase()) {
                        is DomainResult.Success -> {
                            logger.d(TAG, "Account linked successfully")
                        }
                        is DomainResult.Failure -> {
                            logger.w(TAG, "Link account failed (non-blocking): ${linkResult.error}")
                        }
                    }

                    _uiState.value = AuthUiState.Success

                    // ✅ Проверяем есть ли активный пет
                    val activePetId = sessionRepository.getActivePetId()
                    if (activePetId != null) {
                        _event.emit(AuthEvent.NavigateToPet(activePetId))
                    } else {
                        _event.emit(AuthEvent.NavigateToMain)
                    }
                }
                is DomainResult.Failure -> {
                    val resId = result.error.toUiErrorStringRes()
                    _uiState.value = AuthUiState.Error(resId)
                    _event.emit(AuthEvent.ShowError(resId))
                }
            }
        }
    }
}