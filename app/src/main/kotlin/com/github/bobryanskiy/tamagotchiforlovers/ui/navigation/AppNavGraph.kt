package com.github.bobryanskiy.tamagotchiforlovers.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.github.bobryanskiy.tamagotchiforlovers.ui.screens.CreatePetScreen
import com.github.bobryanskiy.tamagotchiforlovers.ui.screens.GameOverScreen
import com.github.bobryanskiy.tamagotchiforlovers.ui.screens.LobbyContainer
import com.github.bobryanskiy.tamagotchiforlovers.ui.screens.StartContainer
import com.github.bobryanskiy.tamagotchiforlovers.ui.screens.GameContainer

@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = "start") {

        composable("start") {
            StartContainer(navController = navController)
        }

        composable("create_pet") {
            CreatePetScreen(navController = navController)
        }

        composable("lobby") {
            LobbyContainer(navController = navController)
        }

        composable("game") {
            GameContainer(navController = navController)
        }

        composable("game_over") {
            GameOverScreen(
                onRestart = {
                    navController.navigate("start") {
                        popUpTo("start") { inclusive = true }
                    }
                }
            )
        }
    }
}