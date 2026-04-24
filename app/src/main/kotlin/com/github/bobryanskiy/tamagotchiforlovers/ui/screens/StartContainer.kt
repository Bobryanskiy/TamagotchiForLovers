package com.github.bobryanskiy.tamagotchiforlovers.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.PairViewModel
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.PetViewModel

@Composable
fun StartContainer(
    navController: NavController,
    pairViewModel: PairViewModel = hiltViewModel(),
    petViewModel: PetViewModel = hiltViewModel()
) {
    // Собираем состояния локально
    val pairState by pairViewModel.uiState.collectAsState()
    val petState by petViewModel.uiState.collectAsState()

    // Определяем, есть ли активная сессия
    val hasActiveSession = pairState != null || petState != null
    val isGameActive = pairState?.status?.name == "ACTIVE"

    StartScreen(
        hasActiveSession = hasActiveSession,
        onNewGame = {
            navController.navigate("create_pet")
        },
        onContinue = {
            if (isGameActive) {
                navController.navigate("game")
            } else {
                navController.navigate("lobby")
            }
        }
    )
}