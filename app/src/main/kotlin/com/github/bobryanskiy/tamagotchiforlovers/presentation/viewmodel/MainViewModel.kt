package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val authButtonState: StateFlow<AuthButtonState> = userRepository.observeCurrentUser()
        .map { user ->
            if (user != null) AuthButtonState.Profile(user.uid)
            else AuthButtonState.Login
        }
        .stateIn(
            scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main),
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AuthButtonState.Loading
        )
}

sealed class AuthButtonState {
    data object Loading : AuthButtonState()
    data object Login : AuthButtonState()
    data class Profile(val userId: String) : AuthButtonState()
}