package com.github.bobryanskiy.tamagotchiforlovers.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
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
                    navController.navigate(AppRoute.Auth)
                },
                onNavigateToProfile = { userId ->
                    navController.navigate(AppRoute.Profile(userId))
                },
                onNavigateToGame = {
                    navController.navigate(AppRoute.CreatePet)
                },
                onNavigateToPairConnect = {
                    navController.navigate(AppRoute.JoinPair)
                }
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
                onNavigateToPair = { id -> navController.navigate(AppRoute.Pair(id)) },
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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(AppRoute.Auth) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
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

        composable<AppRoute.Pair> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.Pair>()
            HostPairScreen(
                petId = route.petId,
                onNavigateBack = { navController.popBackStack() },
                onPairReady = {
                    navController.popBackStack()
                }
            )
        }

        composable<AppRoute.JoinPair> {
            JoinPairScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onJoinedSuccess = { petId ->
                    navController.navigate(AppRoute.Pet(petId = petId)) {
                        popUpTo<AppRoute.JoinPair> { inclusive = true }
                    }
                }
            )
        }
    }
}