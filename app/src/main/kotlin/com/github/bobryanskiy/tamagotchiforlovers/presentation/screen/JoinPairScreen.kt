package com.github.bobryanskiy.tamagotchiforlovers.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bobryanskiy.tamagotchiforlovers.R
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

    val titleText = stringResource(R.string.join_pair_screen_title)
    val backDesc = stringResource(R.string.join_pair_back)
    val searchingText = stringResource(R.string.join_pair_searching)
    val sendingText = stringResource(R.string.join_pair_sending)
    val connectingText = stringResource(R.string.join_pair_connecting)

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is JoinPairUiState.Joined) {
            onJoinedSuccess(state.petId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        titleText,
                        modifier = Modifier.semantics { heading() }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, backDesc)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is JoinPairUiState.Idle -> InviteCodeInputScreen(onSubmit = viewModel::submitInviteCode)
                is JoinPairUiState.Searching -> LoadingContent(message = searchingText)
                is JoinPairUiState.SendingRequest -> LoadingContent(message = sendingText)
                is JoinPairUiState.WaitingForApproval -> WaitingForApprovalContent(
                    pairName = state.pairName,
                    onDismiss = { viewModel.resetState(); onNavigateBack() }
                )
                is JoinPairUiState.Error -> ErrorContent(
                    messageResId = state.messageResId,
                    onRetry = { viewModel.resetState() }
                )
                is JoinPairUiState.Joined -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.semantics {
                                    contentDescription = connectingText
                                }
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                connectingText,
                                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                            )
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

    val titleText = stringResource(R.string.join_pair_title)
    val subtitleText = stringResource(R.string.join_pair_subtitle)
    val labelText = stringResource(R.string.join_pair_invite_label)
    val placeholderText = stringResource(R.string.join_pair_invite_placeholder)
    val connectButtonText = stringResource(R.string.join_pair_connect_btn)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {},
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    titleText,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.semantics { heading() }
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    subtitleText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { code = it.uppercase() },
            label = { Text(labelText) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(placeholderText) }
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { onSubmit(code) },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { role = Role.Button },
            enabled = code.isNotBlank()
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(connectButtonText)
        }
    }
}

@Composable
private fun WaitingForApprovalContent(pairName: String, onDismiss: () -> Unit) {
    val waitingTitle = stringResource(R.string.join_pair_waiting_title)
    val waitingPairName = stringResource(R.string.join_pair_waiting_pair_name, pairName)
    val waitingMessage = stringResource(R.string.join_pair_waiting_message)
    val cancelButtonText = stringResource(R.string.join_pair_cancel_btn)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.semantics {
                contentDescription = waitingTitle
            }
        )
        Spacer(Modifier.height(24.dp))
        Text(
            waitingTitle,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(8.dp))
        Text(
            waitingPairName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
        )
        Spacer(Modifier.height(16.dp))
        Text(
            waitingMessage,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(32.dp))
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.semantics { role = Role.Button }
        ) {
            Text(cancelButtonText)
        }
    }
}

@Composable
private fun LoadingContent(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.clearAndSetSemantics { }
        )
        Spacer(Modifier.height(16.dp))
        Text(message)
    }
}

@Composable
internal fun ErrorContent(messageResId: Int, onRetry: () -> Unit) {
    val message = stringResource(messageResId)
    val tryAgainText = stringResource(R.string.try_again)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.semantics { role = Role.Button }
        ) {
            Text(tryAgainText)
        }
    }
}
