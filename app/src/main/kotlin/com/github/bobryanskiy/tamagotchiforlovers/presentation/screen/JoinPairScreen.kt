package com.github.bobryanskiy.tamagotchiforlovers.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.JoinPairUiState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.JoinPairViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinPairScreen(
    viewModel: JoinPairViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onJoinedSuccess: (petId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Если успешно присоединились → переходим на экран питомца
    if (uiState is JoinPairUiState.Joined) {
        LaunchedEffect(Unit) {
            val petId = (uiState as JoinPairUiState.Joined).petId
            onJoinedSuccess(petId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.join_pair_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.join_pair_back))
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {

                is JoinPairUiState.Idle -> {
                    InviteCodeInputScreen(onSubmit = viewModel::submitInviteCode)
                }

                is JoinPairUiState.Searching -> {
                    LoadingContent(stringResource(R.string.join_pair_searching))
                }

                is JoinPairUiState.SendingRequest -> {
                    LoadingContent(stringResource(R.string.join_pair_sending))
                }

                is JoinPairUiState.WaitingForApproval -> {
                    WaitingForApprovalContent(
                        pairName = state.pairName,
                        onDismiss = {
                            viewModel.resetState()
                            onNavigateBack()
                        }
                    )
                }

                is JoinPairUiState.Error -> {
                    ErrorContent(
                        messageResId = state.messageResId,
                        onRetry = { viewModel.resetState() }
                    )
                }

                is JoinPairUiState.Joined -> {
                    LaunchedEffect(Unit) {
                        onJoinedSuccess(state.petId)
                    }
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(16.dp))
                            Text(stringResource(R.string.join_pair_connecting))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InviteCodeInputScreen(onSubmit: (String) -> Unit) {
    var code by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.join_pair_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.join_pair_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { code = it.uppercase() },
            label = { Text(stringResource(R.string.join_pair_invite_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(stringResource(R.string.join_pair_invite_placeholder)) }
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { onSubmit(code) },
            modifier = Modifier.fillMaxWidth(),
            enabled = code.isNotBlank()
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.join_pair_connect_btn))
        }
    }
}

@Composable
private fun WaitingForApprovalContent(pairName: String, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(24.dp))

        Text(
            stringResource(R.string.join_pair_waiting_title),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Пара: $pairName",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            stringResource(R.string.join_pair_waiting_message),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(32.dp))

        OutlinedButton(onClick = onDismiss) {
            Text(stringResource(R.string.join_pair_cancel_btn))
        }
    }
}

@Composable
private fun LoadingContent(message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text(message)
    }
}

@Composable
private fun ErrorContent(messageResId: Int, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(messageResId),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.btn_retry))
        }
    }
}