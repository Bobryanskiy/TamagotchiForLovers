package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pair
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PendingRequest
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PairManagementUiState {
    object Loading : PairManagementUiState
    data class Active(val pair: Pair, val requests: List<PendingRequest>) : PairManagementUiState
    data class Error(val message: String) : PairManagementUiState
}

@HiltViewModel
class PairManagementViewModel @Inject constructor(
    private val pairRepository: PairRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PairManagementUiState>(PairManagementUiState.Loading)
    val uiState: StateFlow<PairManagementUiState> = _uiState.asStateFlow()

    fun observePairAndRequests(pairId: String) {
//        viewModelScope.launch {
//            combine(
//                pairRepository.observePair(pairId),
//                pairRepository.getPendingRequestsFlow(pairId)
//            ) { pair, requests ->
//                if (pair == null) return@combine PairManagementUiState.Error("Pair not found")
//                PairManagementUiState.Active(pair, requests)
//            }.collect { _uiState.value = it }
//        }
    }

    fun acceptRequest(pairId: String, guestId: String) {
        viewModelScope.launch {
//            pairRepository.acceptPlayer(pairId, guestId)
        }
    }

    fun rejectRequest(pairId: String, guestId: String) {
        viewModelScope.launch {
//            pairRepository.removePendingRequest(pairId, guestId)
        }
    }
}