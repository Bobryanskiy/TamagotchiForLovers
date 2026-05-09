package com.github.bobryanskiy.tamagotchiforlovers.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.github.bobryanskiy.tamagotchiforlovers.ui.screen.BootScreen
import com.github.bobryanskiy.tamagotchiforlovers.ui.screen.MainScreen
import com.github.bobryanskiy.tamagotchiforlovers.ui.screen.PetScreen
import com.github.bobryanskiy.tamagotchiforlovers.ui.screen.AuthScreen
import com.github.bobryanskiy.tamagotchiforlovers.ui.screen.PuzzleScreen
import com.github.bobryanskiy.tamagotchiforlovers.ui.screen.CreatePairScreen
import com.github.bobryanskiy.tamagotchiforlovers.ui.screen.JoinRequestsScreen
import com.github.bobryanskiy.tamagotchiforlovers.ui.screen.PetActionType

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Boot,
        modifier = modifier
    ) {
        composable<AppRoute.Boot> {
            BootScreen(
                onNavigateToMain = {
                    navController.navigate(AppRoute.Main) {
                        popUpTo<AppRoute.Boot> { inclusive = true }
                    }
                },
                onNavigateToPet = { petId ->
                    navController.navigate(AppRoute.Pet) {
                        popUpTo<AppRoute.Boot> { inclusive = true }
                    }
                },
                onNavigateToAuth = {
                    navController.navigate(AppRoute.Auth)
                }
            )
        }
        composable<AppRoute.Main> {
            MainScreen(
                navController = navController,
                onNavigateToAuth = {
                    navController.navigate(AppRoute.Auth)
                },
                onNavigateToGame = {
                    // TODO: Navigate to game screen
                },
                onNavigateToPairConnect = {
                    // TODO: Navigate to pair connect screen
                }
            )
        }
        composable<AppRoute.Pet> {
            PetScreen(
                navController = navController,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPuzzle = { petId, actionType ->
                    val actionTypeName = when (actionType) {
                        is PetActionType.Feed -> "Feed"
                        is PetActionType.Rest -> "Rest"
                        is PetActionType.Clean -> "Clean"
                        is PetActionType.Play -> "Play"
                    }
                    navController.navigate(AppRoute.Puzzle(petId, actionTypeName))
                },
                onNavigateToCreatePair = { petId ->
                    navController.navigate(AppRoute.CreatePair(petId))
                },
                onNavigateToJoinRequests = { pairId ->
                    navController.navigate(AppRoute.JoinRequests(pairId))
                }
            )
        }
        composable<AppRoute.Auth> {
            AuthScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable<AppRoute.Puzzle> { backStackEntry ->
            val route = backStackEntry.arguments as AppRoute.Puzzle
            val actionType = when (route.actionType) {
                "Feed" -> PetActionType.Feed
                "Rest" -> PetActionType.Rest
                "Clean" -> PetActionType.Clean
                "Play" -> PetActionType.Play
                else -> PetActionType.Feed
            }
            PuzzleScreen(
                navController = navController,
                petId = route.petId,
                actionType = actionType,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable<AppRoute.CreatePair> { backStackEntry ->
            val route = backStackEntry.arguments as AppRoute.CreatePair
            CreatePairScreen(
                navController = navController,
                petId = route.petId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable<AppRoute.JoinRequests> { backStackEntry ->
            val route = backStackEntry.arguments as AppRoute.JoinRequests
            JoinRequestsScreen(
                navController = navController,
                pairId = route.pairId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}