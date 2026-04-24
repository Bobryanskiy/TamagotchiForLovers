package com.github.bobryanskiy.tamagotchiforlovers.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.PairViewModel
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.PetViewModel

@Composable
fun GameContainer(
    navController: NavController,
    viewModel: PetViewModel = hiltViewModel(),
    pairViewModel: PairViewModel = hiltViewModel()
) {
    // 1. Собираем состояние ТОЛЬКО здесь
    val pet by viewModel.uiState.collectAsState()
    val pair by pairViewModel.uiState.collectAsState()

    // 2. Обрабатываем события навигации внутри контейнера
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is PetViewModel.UiEvent.NavigateToGameOver -> {
                    // Передаем статус через аргументы или просто идем на экран проигрыша
                    navController.navigate("game_over")
                }
                is PetViewModel.UiEvent.NavigateToHome -> {
                    navController.navigate("start") {
                        popUpTo("start") { inclusive = true }
                    }
                }
                else -> {}
            }
        }
    }

    // Обработка событий от PairViewModel
    LaunchedEffect(Unit) {
        pairViewModel.uiEvent.collect { event ->
            when (event) {
                is PairViewModel.UiEvent.NavigateToLobby -> {
                    navController.navigate("lobby") {
                        popUpTo("game") { inclusive = false }
                    }
                }
                else -> {}
            }
        }
    }

    // 3. Рисуем экран
    GameScreen(
        pet = pet,
        onAction = viewModel::onAction,
        onAbandon = viewModel::abandonPet,
        onCreatePair = {
            navController.navigate("create_pair")
        }
    )
}