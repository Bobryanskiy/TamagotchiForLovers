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
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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
    onNavigateToSettings: () -> Unit,
    onNavigateToGame: () -> Unit,
    onNavigateToPairConnect: () -> Unit
) {
    val authState by viewModel.authButtonState.collectAsStateWithLifecycle()

    val appName = stringResource(R.string.app_name)
    val settingsDesc = stringResource(R.string.settings_title)
    val loginDesc = stringResource(R.string.login)
    val profileDesc = stringResource(R.string.menu_profile)
    val loadingDesc = stringResource(R.string.loading)
    val welcomeMessage = stringResource(R.string.welcome_message)
    val startGameText = stringResource(R.string.start_game)
    val connectToPairText = stringResource(R.string.connect_to_pair)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        appName,
                        modifier = Modifier.semantics { heading() }
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = settingsDesc
                        )
                    }

                    when (authState) {
                        is AuthButtonState.Login -> {
                            IconButton(onClick = onNavigateToAuth) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = loginDesc
                                )
                            }
                        }
                        is AuthButtonState.Profile -> {
                            IconButton(onClick = onNavigateToProfile) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = profileDesc
                                )
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
                    CircularProgressIndicator(
                        modifier = Modifier.semantics {
                            contentDescription = loadingDesc
                        }
                    )
                }
            }
            else -> {
                MainContent(
                    modifier = Modifier.padding(padding).padding(24.dp),
                    onStartGame = onNavigateToGame,
                    onPairConnect = onNavigateToPairConnect,
                    welcomeMessage = welcomeMessage,
                    startGameText = startGameText,
                    connectToPairText = connectToPairText
                )
            }
        }
    }
}

@Composable
private fun MainContent(
    modifier: Modifier = Modifier,
    onStartGame: () -> Unit,
    onPairConnect: () -> Unit,
    welcomeMessage: String,
    startGameText: String,
    connectToPairText: String
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = welcomeMessage,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() }
        )

        Button(
            onClick = onStartGame,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .semantics { role = Role.Button }
        ) {
            Text(startGameText)
        }

        OutlinedButton(
            onClick = onPairConnect,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .semantics { role = Role.Button }
        ) {
            Text(connectToPairText)
        }
    }
}
