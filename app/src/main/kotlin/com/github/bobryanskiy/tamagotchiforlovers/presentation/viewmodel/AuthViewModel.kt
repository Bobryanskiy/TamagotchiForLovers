package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.UserResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.LinkAccountUseCase
import com.github.bobryanskiy.tamagotchiforlovers.presentation.mapper.toUiErrorStringRes
import com.github.bobryanskiy.tamagotchiforlovers.util.ValidationUtils
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
    data object NavigateToBoot : AuthEvent
    data class ShowError(val messageResId: Int) : AuthEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val linkAccountUseCase: LinkAccountUseCase,
    private val sessionRepository: SessionRepository,
    private val petRepository: PetRepository,
    private val logger: AppLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val event: SharedFlow<AuthEvent> = _event.asSharedFlow()

    private val _linkConflict = MutableStateFlow<Pair<Pet, Pet>?>(null)
    val linkConflict: StateFlow<Pair<Pet, Pet>?> = _linkConflict.asStateFlow()

    companion object {
        private const val TAG = "AuthVM"
    }

    private fun executeAuthAction(authOperation: suspend () -> UserResult<Unit>) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            when (val result = authOperation()) {
                is DomainResult.Success -> {
                    when (val linkResult = linkAccountUseCase()) {
                        is DomainResult.Success -> {
                            when (val data = linkResult.data) {
                                is LinkAccountUseCase.LinkResult.Success -> {
                                    logger.d(TAG, "Account linked successfully")
                                }
                                is LinkAccountUseCase.LinkResult.Conflict -> {
                                    _linkConflict.value = Pair(data.localPet, data.remotePet)
                                    logger.w(TAG, "Link conflict detected")
                                }
                            }
                        }
                        is DomainResult.Failure -> {
                            logger.w(TAG, "Link failed: ${linkResult.error}")
                        }
                    }
                    _uiState.value = AuthUiState.Success
                    _event.emit(AuthEvent.NavigateToBoot)
                }
                is DomainResult.Failure -> {
                    _uiState.value = AuthUiState.Error(result.error.toUiErrorStringRes())
                    _event.emit(AuthEvent.ShowError(result.error.toUiErrorStringRes()))
                }
            }
        }
    }

    fun resolveConflict(chooseLocal: Boolean) {
        val conflict = _linkConflict.value ?: return
        viewModelScope.launch {
            val winner = if (chooseLocal) conflict.first else conflict.second
            petRepository.savePet(winner)
            sessionRepository.saveActivePetId(winner.id)
            _linkConflict.value = null
        }
    }

    fun login(email: String, password: String) {
        val emailError = ValidationUtils.getEmailErrorResId(email)
        val passwordError = ValidationUtils.getPasswordErrorResId(password)

        if (emailError != null || passwordError != null) {
            _uiState.value = AuthUiState.Error(
                emailError ?: passwordError ?: R.string.error_unknown
            )
            return
        }

        executeAuthAction { authRepository.signIn(email, password) }
    }

    fun register(email: String, password: String) {
        val emailError = ValidationUtils.getEmailErrorResId(email)
        val passwordError = ValidationUtils.getPasswordErrorResId(password)

        if (emailError != null || passwordError != null) {
            _uiState.value = AuthUiState.Error(
                emailError ?: passwordError ?: R.string.error_unknown
            )
            return
        }

        executeAuthAction { authRepository.signUp(email, password) }
    }
}
