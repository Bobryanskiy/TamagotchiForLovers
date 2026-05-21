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
                onNavigateToAuth = {
                    navController.navigate(AppRoute.Auth) {
                        popUpTo<AppRoute.Boot> { inclusive = true }
                    }
                },
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
                onNavigateToAuth = {
                    navController.navigate(AppRoute.Auth) {
                        popUpTo<AppRoute.Main> { inclusive = false }
                    }
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
                onNavigateToCreatePair = { id -> navController.navigate(AppRoute.CreatePair(id)) },
                onNavigateToProfile = { id ->
                    navController.navigate(AppRoute.Profile(id))
                },
                onNavigateToMain = {
                    navController.navigate(AppRoute.Main) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<AppRoute.Profile> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.Profile>()
            ProfileScreen(
                petId = route.petId,
                onNavigateBack = { navController.popBackStack() }
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

        composable<AppRoute.CreatePair> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.CreatePair>()
            CreatePairScreen(
                petId = route.petId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<AppRoute.JoinRequests> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.JoinRequests>()
            JoinRequestsScreen(
                pairId = route.pairId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<AppRoute.PairConnect> {
            PairConnectScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPet = { petId ->
                    navController.navigate(AppRoute.Pet(petId)) {
                        popUpTo<AppRoute.Main> { inclusive = false }
                    }
                }
            )
        }
    }
}