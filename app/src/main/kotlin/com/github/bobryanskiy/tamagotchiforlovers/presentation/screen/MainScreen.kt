package com.github.bobryanskiy.tamagotchiforlovers.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.AuthButtonState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToAuth: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToGame: () -> Unit,
    onNavigateToPairConnect: () -> Unit
) {
    val authState by viewModel.authButtonState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    when (val state = authState) {
                        is AuthButtonState.Login -> {
                            IconButton(onClick = onNavigateToAuth) {
                                Icon(Icons.Default.Person, stringResource(R.string.login))
                            }
                        }
                        is AuthButtonState.Profile -> {
                            IconButton(onClick = onNavigateToProfile) {
                                Icon(Icons.Default.AccountCircle, "Profile")
                            }
                        }
                        is AuthButtonState.Loading -> {}
                    }
                }
            )
        }
    ) { padding ->
        when (authState) {
            is AuthButtonState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                MainContent(
                    modifier = Modifier.padding(padding).padding(24.dp),
                    onStartGame = onNavigateToGame,
                    onPairConnect = onNavigateToPairConnect
                )
            }
        }
    }
}

@Composable
private fun MainContent(
    modifier: Modifier = Modifier,
    onStartGame: () -> Unit,
    onPairConnect: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Spacer(Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.welcome_message),
            style = MaterialTheme.typography.headlineSmall
        )

        Button(
            onClick = onStartGame,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(stringResource(R.string.start_game))
        }

        OutlinedButton(
            onClick = onPairConnect,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(stringResource(R.string.connect_to_pair))
        }
    }
}