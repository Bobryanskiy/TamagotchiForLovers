package com.github.bobryanskiy.tamagotchiforlovers.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.StartViewModel

@Composable
fun StartContainer(
    navController: NavController,
    viewModel: StartViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Обновляем состояние при входе на экран
    LaunchedEffect(Unit) {
        viewModel.refreshState()
    }

    // Обработка событий навигации
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                else -> {} // Пока без специальной навигации
            }
        }
    }

    StartScreen(
        hasActiveSession = !state.isLoading && (state.hasActivePet || state.hasActivePair),
        onNewGame = {
            if (state.hasActivePet && !state.hasActivePair) {
                navController.navigate("create_pair")
            } else {
                navController.navigate("create_pet")
            }
        },
        onContinue = {
            if (state.isGameActive) {
                navController.navigate("game")
            } else if (state.hasActivePair) {
                navController.navigate("lobby")
            } else if (state.hasActivePet) {
                navController.navigate("game")
            }
        },
        onJoinGame = {
            navController.navigate("join")
        },
        onAccountClick = {
            // TODO: Навигация на экран аккаунта
        }
    )
}