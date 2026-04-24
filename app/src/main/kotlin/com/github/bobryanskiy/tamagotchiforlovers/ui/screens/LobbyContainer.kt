package com.github.bobryanskiy.tamagotchiforlovers.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PairStatus
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.PairViewModel

@Composable
fun LobbyContainer(
    navController: NavController,
    viewModel: PairViewModel = hiltViewModel()
) {
    // 1. Собираем состояние пары
    val pair by viewModel.uiState.collectAsState()

    // 2. Логика переходов
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is PairViewModel.UiEvent.NavigateToGame -> {
                    navController.navigate("game")
                }
                is PairViewModel.UiEvent.NavigateToHome -> {
                    navController.navigate("start") {
                        popUpTo("start") { inclusive = true }
                    }
                }
                else -> {}
            }
        }
    }

    // Авто-переход в игру, если статус сменился на ACTIVE
    LaunchedEffect(pair?.status) {
        if (pair?.status == PairStatus.ACTIVE) {
            navController.navigate("game")
        }
    }

    // 3. Рисуем экран
    LobbyScreen(
        pair = pair,
        onAccept = viewModel::acceptRequest,
        onLeave = viewModel::leaveSession
    )
}