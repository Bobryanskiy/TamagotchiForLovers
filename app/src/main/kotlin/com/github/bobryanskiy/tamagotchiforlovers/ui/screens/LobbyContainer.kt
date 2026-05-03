package com.github.bobryanskiy.tamagotchiforlovers.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.ui.state.UiEvent
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.LobbyViewModel

@Composable
fun LobbyContainer(
    navController: NavController,
    viewModel: LobbyViewModel = hiltViewModel()
) {
    // 1. Собираем состояние пары
    val state by viewModel.uiState.collectAsState()

    // 2. Логика переходов
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.NavigateToGame -> {
                    navController.navigate("game")
                }
                is UiEvent.NavigateToHome -> {
                    navController.navigate("start") {
                        popUpTo("start") { inclusive = true }
                    }
                }
                is UiEvent.InviteCodeGenerated -> {
                    // Код уже отображается в UI через обновление состояния
                }
                is UiEvent.ShowError -> {
                    // Можно показать Snackbar или Toast
                }
                else -> {}
            }
        }
    }

    // Авто-переход в игру, если статус сменился на ACTIVE
    LaunchedEffect(state.status) {
        if (state.status == PairStatus.ACTIVE) {
            navController.navigate("game") {
                popUpTo("lobby") { inclusive = true }
            }
        }
    }

    // 3. Рисуем экран
    LobbyScreen(
        state = state,
        onAccept = viewModel::acceptRequest,
        onLeave = viewModel::leaveSession,
        onGenerateCode = viewModel::generateInviteCode
    )
}