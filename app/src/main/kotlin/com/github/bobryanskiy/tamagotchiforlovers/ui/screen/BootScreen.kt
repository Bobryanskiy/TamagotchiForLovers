package com.github.bobryanskiy.tamagotchiforlovers.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.BootUiState
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.BootViewModel

@Composable
fun BootScreen(
    viewModel: BootViewModel = hiltViewModel(),
    onNavigateToMain: () -> Unit,
    onNavigateToPet: (String) -> Unit,
    onNavigateToAuth: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is BootUiState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        is BootUiState.NavigateToMain -> onNavigateToMain()
        is BootUiState.NavigateToPet -> onNavigateToPet(state.petId)
        is BootUiState.NavigateToAuth -> onNavigateToAuth()
    }
}
