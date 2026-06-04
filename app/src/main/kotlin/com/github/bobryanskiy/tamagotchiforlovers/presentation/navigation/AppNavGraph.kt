package com.github.bobryanskiy.tamagotchiforlovers.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Loadable
import com.github.bobryanskiy.tamagotchiforlovers.presentation.screen.AuthScreen
import com.github.bobryanskiy.tamagotchiforlovers.presentation.screen.BootScreen
import com.github.bobryanskiy.tamagotchiforlovers.presentation.screen.CreatePairScreen
import com.github.bobryanskiy.tamagotchiforlovers.presentation.screen.CreatePetScreen
import com.github.bobryanskiy.tamagotchiforlovers.presentation.screen.EditNicknameScreen
import com.github.bobryanskiy.tamagotchiforlovers.presentation.screen.JoinPairScreen
import com.github.bobryanskiy.tamagotchiforlovers.presentation.screen.MainScreen
import com.github.bobryanskiy.tamagotchiforlovers.presentation.screen.PairActiveScreen
import com.github.bobryanskiy.tamagotchiforlovers.presentation.screen.PairWaitingScreen
import com.github.bobryanskiy.tamagotchiforlovers.presentation.screen.PetScreen
import com.github.bobryanskiy.tamagotchiforlovers.presentation.screen.ProfileScreen
import com.github.bobryanskiy.tamagotchiforlovers.presentation.screen.RenamePetScreen
import com.github.bobryanskiy.tamagotchiforlovers.presentation.screen.SettingsScreen
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.AppViewModel

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    appViewModel: AppViewModel,
) {
    val activePetIdState by appViewModel.activePetId.collectAsStateWithLifecycle()

    LaunchedEffect(activePetIdState) {
        when (val state = activePetIdState) {
            is Loadable.Loading -> {
                return@LaunchedEffect
            }
            is Loadable.Loaded -> {
                if (state.value == null) {
                    val currentRoute = navController.currentDestination?.route ?: ""
                    val safeRoutes = listOf("Main", "Auth", "Boot", "CreatePet")
                    val isOnSafeRoute = safeRoutes.any { currentRoute.contains(it) }

                    if (!isOnSafeRoute) {
                        navController.navigate(AppRoute.Main) {
                            popUpTo<AppRoute.Main> { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }

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
                onNavigateToSettings = { navController.navigate(AppRoute.Settings) },
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
                    navController.navigate(AppRoute.Auth) {
                        popUpTo<AppRoute.Profile> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToSettings = {navController.navigate(AppRoute.Settings)},
                onNavigateToEditNickname = { navController.navigate(AppRoute.EditNickname) }
            )
        }

        composable<AppRoute.Auth> {
            AuthScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<AppRoute.Settings> {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable<AppRoute.CreatePair> { backStackEntry ->
            backStackEntry.toRoute<AppRoute.CreatePair>()
            CreatePairScreen(
                onNavigateBack = { navController.popBackStack() },
                onPairCreated = { pairId ->
                    navController.navigate(AppRoute.PairWaiting(pairId)) {
                        popUpTo<AppRoute.CreatePair> { inclusive = true }
                    }
                }
            )
        }

        composable<AppRoute.PairWaiting> { backStackEntry ->
            backStackEntry.toRoute<AppRoute.PairWaiting>()
            PairWaitingScreen(
                onNavigateBack = { navController.popBackStack() },
                onPairActivated = {
                    navController.popBackStack()
                }
            )
        }

        composable<AppRoute.PairActive> { backStackEntry ->
            backStackEntry.toRoute<AppRoute.PairActive>()
            PairActiveScreen(
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
            backStackEntry.toRoute<AppRoute.RenamePet>()
            RenamePetScreen(
                onNavigateBack = { navController.popBackStack() },
                onRenamed = { navController.popBackStack() }
            )
        }

        composable<AppRoute.EditNickname> {
            EditNicknameScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
