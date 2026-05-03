package com.example.petgame.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.petgame.ui.screens.home.HomeScreen
import com.example.petgame.ui.screens.petCreation.PetCreationScreen
import com.example.petgame.ui.screens.petMain.PetMainScreen
import com.example.petgame.ui.screens.login.LoginScreen
import com.example.petgame.ui.screens.connection.ConnectionScreen
// MiniGames экран будет добавлен позже

/**
 * Главный навигационный граф приложения.
 * Определяет все доступные экраны и переходы между ними.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Главный экран
        composable(Screen.Home.route) {
            HomeScreen(
                onStartNewGame = { navController.navigate(Screen.PetCreation.route) },
                onContinueGame = { petId -> 
                    navController.navigate(Screen.PetMain.createRoute(petId))
                },
                onConnectToGame = { navController.navigate(Screen.Connection.route) },
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onExitApp = { /* Логика выхода из приложения */ }
            )
        }

        // Экран создания питомца
        composable(Screen.PetCreation.route) {
            PetCreationScreen(
                onPetCreated = { petId ->
                    navController.navigate(Screen.PetMain.createRoute(petId)) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Экран основного геймплея с питомцем
        composable(
            route = Screen.PetMain.route,
            arguments = listOf(
                navArgument("petId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getInt("petId") ?: return@composable
            PetMainScreen(
                petId = petId,
                onNavigateToMiniGames = { statType ->
                    navController.navigate(Screen.MiniGames.createRoute(statType))
                },
                onBackToHome = { navController.popBackStack(Screen.Home.route, false) }
            )
        }

        // Экран входа в аккаунт
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // Экран подключения к онлайн-игре
        composable(Screen.Connection.route) {
            ConnectionScreen(
                onConnectionSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // Экран мини-игр (будет реализован позже)
        composable(
            route = Screen.MiniGames.route,
            arguments = listOf(
                navArgument("statType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val statType = backStackEntry.arguments?.getString("statType") ?: return@composable
            // Заглушка для экрана мини-игр
            // MiniGamesScreen(
            //     statType = statType,
            //     onGameComplete = { navController.popBackStack() },
            //     onBack = { navController.popBackStack() }
            // )
        }
    }
}
