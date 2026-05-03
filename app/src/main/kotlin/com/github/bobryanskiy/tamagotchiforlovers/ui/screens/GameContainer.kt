package com.github.bobryanskiy.tamagotchiforlovers.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.bobryanskiy.tamagotchiforlovers.ui.state.UiEvent
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.GameViewModel

@Composable
fun GameContainer(
    navController: NavController,
    viewModel: GameViewModel = hiltViewModel()
) {
    // 1. Собираем состояние ТОЛЬКО здесь
    val state by viewModel.uiState.collectAsState()

    // 2. Обрабатываем события навигации внутри контейнера
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.NavigateToGameOver -> {
                    navController.navigate("game_over")
                }
                is UiEvent.NavigateToHome -> {
                    navController.navigate("start") {
                        popUpTo("start") { inclusive = true }
                    }
                }
                is UiEvent.ShowError -> {
                    // Можно показать Snackbar или Toast
                }
                else -> {}
            }
        }
    }

    // 3. Рисуем экран
    GameScreen(
        state = state,
        onAction = viewModel::onAction,
        onAbandon = viewModel::abandonPet,
        onCreatePair = {
            navController.navigate("create_pair")
        }
    )
}