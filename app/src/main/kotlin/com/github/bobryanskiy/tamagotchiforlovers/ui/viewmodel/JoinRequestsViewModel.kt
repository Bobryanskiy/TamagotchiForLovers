package com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PendingRequest
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.AcceptPlayerUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.GetPendingRequestsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface JoinRequestsUiState {
    data object Loading : JoinRequestsUiState
    data class Content(val requests: List<PendingRequest>) : JoinRequestsUiState
    data class Error(val message: String) : JoinRequestsUiState
}

@HiltViewModel
class JoinRequestsViewModel @Inject constructor(
    private val getPendingRequestsUseCase: GetPendingRequestsUseCase,
    private val acceptPlayerUseCase: AcceptPlayerUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<JoinRequestsUiState>(JoinRequestsUiState.Loading)
    val uiState: StateFlow<JoinRequestsUiState> = _uiState.asStateFlow()

    fun loadRequests(pairId: String) {
        viewModelScope.launch {
            _uiState.value = JoinRequestsUiState.Loading
            when (val result = getPendingRequestsUseCase(pairId)) {
                is DomainResult.Success -> {
                    _uiState.value = JoinRequestsUiState.Content(result.data)
                }
                is DomainResult.Failure -> {
                    _uiState.value = JoinRequestsUiState.Error("Ошибка загрузки запросов: ${result.error}")
                }
            }
        }
    }

    fun acceptRequest(pairId: String, guestId: String) {
        viewModelScope.launch {
            when (val result = acceptPlayerUseCase(pairId, guestId)) {
                is DomainResult.Success -> {
                    loadRequests(pairId) // Reload requests after accepting
                }
                is DomainResult.Failure -> {
                    _uiState.value = JoinRequestsUiState.Error("Ошибка принятия запроса: ${result.error}")
                }
            }
        }
    }

    fun rejectRequest(pairId: String, guestId: String) {
        // For now, just reload without the request
        // In a real implementation, you might want to explicitly remove the pending request
        loadRequests(pairId)
    }
}
