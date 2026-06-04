package com.github.bobryanskiy.tamagotchiforlovers.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.presentation.navigation.AppRoute
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.BootViewModel

@Composable
fun BootScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToPet: (String) -> Unit,
    viewModel: BootViewModel = hiltViewModel()
) {
    val navigationState by viewModel.navigationState.collectAsStateWithLifecycle()
    val appLaunchingDesc = stringResource(R.string.app_launching)

    LaunchedEffect(navigationState) {
        val route = navigationState ?: return@LaunchedEffect
        when (route) {
            is AppRoute.Main -> onNavigateToMain()
            is AppRoute.Pet -> onNavigateToPet(route.petId)
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics {
                contentDescription = appLaunchingDesc
            },
        contentAlignment = Alignment.Center
    ) {
//         Image(
//             painter = painterResource(),
//             contentDescription = null,
//             modifier = Modifier.size(120.dp)
//         )
    }
}
