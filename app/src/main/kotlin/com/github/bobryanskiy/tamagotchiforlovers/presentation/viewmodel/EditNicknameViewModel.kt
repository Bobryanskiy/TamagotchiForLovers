package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.util.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface EditNicknameUiState {
    data object Idle : EditNicknameUiState
    data class Loaded(val currentNickname: String) : EditNicknameUiState
    data object Loading : EditNicknameUiState
    data object Success : EditNicknameUiState
    data class Error(@param:StringRes val messageResId: Int) : EditNicknameUiState
}

@HiltViewModel
class EditNicknameViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditNicknameUiState>(EditNicknameUiState.Idle)
    val uiState: StateFlow<EditNicknameUiState> = _uiState.asStateFlow()

    private val _nickname = MutableStateFlow("")
    val nickname: StateFlow<String> = _nickname.asStateFlow()

    init {
        loadCurrentNickname()
    }

    private fun loadCurrentNickname() {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId() ?: run {
                _uiState.value = EditNicknameUiState.Error(R.string.error_not_authenticated)
                return@launch
            }
            val current = userRepository.getUserNickname(uid) ?: ""
            _nickname.value = current
            _uiState.value = EditNicknameUiState.Loaded(current)
        }
    }

    fun onNicknameChange(value: String) {
        _nickname.value = value
        if (_uiState.value is EditNicknameUiState.Error) {
            _uiState.value = EditNicknameUiState.Idle
        }
    }

    fun save() {
        val uid = authRepository.getCurrentUserId() ?: run {
            _uiState.value = EditNicknameUiState.Error(R.string.error_not_authenticated)
            return
        }
        val trimmed = _nickname.value.trim()

        val errorResId = ValidationUtils.getNicknameErrorResId(trimmed)
        if (errorResId != null) {
            _uiState.value = EditNicknameUiState.Error(errorResId)
            return
        }

        viewModelScope.launch {
            _uiState.value = EditNicknameUiState.Loading
            when (userRepository.updateNickname(uid, trimmed)) {
                is DomainResult.Success -> _uiState.value = EditNicknameUiState.Success
                is DomainResult.Failure -> _uiState.value = EditNicknameUiState.Error(R.string.error_unknown)
            }
        }
    }
}