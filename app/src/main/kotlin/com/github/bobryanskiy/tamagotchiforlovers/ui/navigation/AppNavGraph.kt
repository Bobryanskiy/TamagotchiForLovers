package com.github.bobryanskiy.tamagotchiforlovers.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.github.bobryanskiy.tamagotchiforlovers.ui.screen.AuthScreen
import com.github.bobryanskiy.tamagotchiforlovers.ui.screen.BootScreen
import com.github.bobryanskiy.tamagotchiforlovers.ui.screen.CreatePairScreen
import com.github.bobryanskiy.tamagotchiforlovers.ui.screen.CreatePetScreen
import com.github.bobryanskiy.tamagotchiforlovers.ui.screen.JoinRequestsScreen
import com.github.bobryanskiy.tamagotchiforlovers.ui.screen.MainScreen
import com.github.bobryanskiy.tamagotchiforlovers.ui.screen.PairConnectScreen
import com.github.bobryanskiy.tamagotchiforlovers.ui.screen.PetActionType
import com.github.bobryanskiy.tamagotchiforlovers.ui.screen.PetScreen
import com.github.bobryanskiy.tamagotchiforlovers.ui.screen.PuzzleScreen

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
                    navController.navigate(AppRoute.CreatePet)
                },
                onNavigateToPairConnect = {
                    navController.navigate(AppRoute.PairConnect)
                }
            )
        }
        composable<AppRoute.CreatePet> {
            CreatePetScreen(
                navController = navController,
                onNavigateToPet = { petId ->
                    navController.navigate(AppRoute.Pet) {
                        popUpTo<AppRoute.Main> { inclusive = false }
                    }
                }
            )
        }
        composable<AppRoute.Pet> { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId") ?: ""
            PetScreen(
                navController = navController,
                petId = petId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPuzzle = { id, actionType ->
                    val actionTypeString = when (actionType) {
                        is PetActionType.Feed -> "Feed"
                        is PetActionType.Rest -> "Rest"
                        is PetActionType.Clean -> "Clean"
                        is PetActionType.Play -> "Play"
                    }
                    navController.navigate(AppRoute.Puzzle(id, actionTypeString))
                },
                onNavigateToCreatePair = { id ->
                    navController.navigate(AppRoute.CreatePair(id))
                },
                onNavigateToJoinRequests = { pairId ->
                    navController.navigate(AppRoute.JoinRequests(pairId))
                }
            )
        }
        composable<AppRoute.Auth> {
            AuthScreen(
                navController = navController,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable<AppRoute.Puzzle> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.Puzzle>()
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
            val route = backStackEntry.toRoute<AppRoute.CreatePair>()
            CreatePairScreen(
                navController = navController,
                petId = route.petId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable<AppRoute.JoinRequests> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.JoinRequests>()
            JoinRequestsScreen(
                navController = navController,
                pairId = route.pairId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable<AppRoute.PairConnect> {
            PairConnectScreen(
                navController = navController,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPet = { petId ->
                    navController.navigate(AppRoute.Pet) {
                        popUpTo<AppRoute.Main> { inclusive = false }
                    }
                }
            )
        }
    }
}