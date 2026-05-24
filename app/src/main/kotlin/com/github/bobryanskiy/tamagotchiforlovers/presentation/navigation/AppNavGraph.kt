package com.github.bobryanskiy.tamagotchiforlovers.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.github.bobryanskiy.tamagotchiforlovers.presentation.screen.*

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
                    navController.navigate(AppRoute.Pet(petId)) {
                        popUpTo<AppRoute.Boot> { inclusive = true }
                    }
                }
            )
        }

        composable<AppRoute.Main> {
            MainScreen(
                onNavigateToAuth = { navController.navigate(AppRoute.Auth) },
                onNavigateToProfile = { navController.navigate(AppRoute.Profile) },
                onNavigateToGame = { navController.navigate(AppRoute.CreatePet) },
                onNavigateToPairConnect = { navController.navigate(AppRoute.JoinPair) }
            )
        }

        composable<AppRoute.CreatePet> {
            CreatePetScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPet = { petId ->
                    navController.navigate(AppRoute.Pet(petId)) {
                        popUpTo<AppRoute.Main> { inclusive = true }
                    }
                }
            )
        }

        composable<AppRoute.Pet> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.Pet>()
            PetScreen(
                petId = route.petId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreatePair = { petId ->
                    navController.navigate(AppRoute.CreatePair(petId))
                },
                onNavigateToPairWaiting = { pairId ->
                    navController.navigate(AppRoute.PairWaiting(pairId))
                },
                onNavigateToPairActive = { pairId ->
                    navController.navigate(AppRoute.PairActive(pairId))
                },
                onNavigateToProfile = { navController.navigate(AppRoute.Profile) },
                onNavigateToMain = {
                    navController.navigate(AppRoute.Main) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onRenamePet = { petId ->
                    navController.navigate(AppRoute.RenamePet(petId))
                }
            )
        }

        composable<AppRoute.Profile> {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(AppRoute.Auth)
                }
            )
        }

        composable<AppRoute.Auth> {
            AuthScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<AppRoute.CreatePair> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.CreatePair>()
            CreatePairScreen(
                petId = route.petId,
                onNavigateBack = { navController.popBackStack() },
                onPairCreated = { pairId ->
                    navController.navigate(AppRoute.PairWaiting(pairId)) {
                        popUpTo<AppRoute.CreatePair> { inclusive = true }
                    }
                }
            )
        }

        composable<AppRoute.PairWaiting> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.PairWaiting>()
            PairWaitingScreen(
                pairId = route.pairId,
                onNavigateBack = { navController.popBackStack() },
                onPairActivated = {
                    navController.popBackStack()
                }
            )
        }

        composable<AppRoute.PairActive> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.PairActive>()
            PairActiveScreen(
                pairId = route.pairId,
                onNavigateBack = { navController.popBackStack() },
                onSessionEnded = {
                    navController.popBackStack()
                }
            )
        }

        composable<AppRoute.JoinPair> {
            JoinPairScreen(
                onNavigateBack = { navController.popBackStack() },
                onJoinedSuccess = { petId ->
                    navController.navigate(AppRoute.Pet(petId = petId)) {}
                }
            )
        }

        composable<AppRoute.RenamePet> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.RenamePet>()
            RenamePetScreen(
                petId = route.petId,
                onNavigateBack = { navController.popBackStack() },
                onRenamed = { navController.popBackStack() }
            )
        }
    }
}