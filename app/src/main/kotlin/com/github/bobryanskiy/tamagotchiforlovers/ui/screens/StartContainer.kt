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
fun StartContainer(
    navController: NavController,
    pairViewModel: PairViewModel = hiltViewModel(),
    petViewModel: PetViewModel = hiltViewModel()
) {
    // Собираем состояния локально
    val pairState by pairViewModel.uiState.collectAsState()
    val petState by petViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        pairViewModel.uiEvent.collect { event ->
            when (event) {
                is PairViewModel.UiEvent.NavigateToGame -> {
                    navController.navigate("game") {
                        popUpTo("start") { inclusive = false }
                    }
                }
                is PairViewModel.UiEvent.NavigateToLobby -> {
                    navController.navigate("lobby") {
                        popUpTo("start") { inclusive = false }
                    }
                }
                else -> {}
            }
        }
    }

    val hasActivePet = petState != null
    val hasActivePair = pairState != null
    val isGameActive = pairState?.status?.name == "ACTIVE"

    StartScreen(
        hasActiveSession = hasActivePet || hasActivePair,
        onNewGame = {
            navController.navigate("create_pet")
        },
        onContinue = {
            if (isGameActive) {
                navController.navigate("game")
            } else if (hasActivePair) {
                navController.navigate("lobby")
            } else if (hasActivePet) {
                // Если есть питомец, но нет пары - идём в игру
                navController.navigate("game")
            }
        }
    )
}