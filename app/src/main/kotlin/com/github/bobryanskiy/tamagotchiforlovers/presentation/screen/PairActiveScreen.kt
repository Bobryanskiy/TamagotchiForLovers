package com.github.bobryanskiy.tamagotchiforlovers.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.PairActiveUiState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.PairActiveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairActiveScreen(
    onNavigateBack: () -> Unit,
    onSessionEnded: () -> Unit,
    viewModel: PairActiveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRenameDialog by remember { mutableStateOf(false) }
    var showKickDialog by remember { mutableStateOf(false) }
    var showEndDialog by remember { mutableStateOf(false) }

    val titleText = stringResource(R.string.pair_active_title)
    val backDesc = stringResource(R.string.back)
    val loadingDesc = stringResource(R.string.loading)
    val endingSessionDesc = stringResource(R.string.ending_session)

    LaunchedEffect(uiState) {
        if (uiState is PairActiveUiState.Ended) onSessionEnded()
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
                is PairActiveUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.semantics {
                                contentDescription = loadingDesc
                            }
                        )
                    }
                }
                is PairActiveUiState.Content -> {
                    PairActiveContent(
                        state = state,
                        onRename = { showRenameDialog = true },
                        onKick = { showKickDialog = true },
                        onEndSession = { showEndDialog = true },
                        onLeave = { viewModel.leaveSession() }
                    )
                }
                is PairActiveUiState.Ended -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.semantics {
                                contentDescription = endingSessionDesc
                            }
                        )
                    }
                }
                is PairActiveUiState.Error -> {
                    ErrorContent(
                        messageResId = state.messageResId,
                        onRetry = onNavigateBack
                    )
                }
            }
        }
    }

    if (showRenameDialog && uiState is PairActiveUiState.Content) {
        RenamePairDialog(
            currentName = (uiState as PairActiveUiState.Content).pairName,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                viewModel.renamePair(newName)
                showRenameDialog = false
            }
        )
    }

    if (showKickDialog) {
        KickConfirmDialog(
            onDismiss = { showKickDialog = false },
            onConfirm = {
                viewModel.kickPartner()
                showKickDialog = false
            }
        )
    }

    if (showEndDialog) {
        EndSessionConfirmDialog(
            onDismiss = { showEndDialog = false },
            onConfirm = {
                viewModel.endSession()
                showEndDialog = false
            }
        )
    }
}

@Composable
private fun RenamePairDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    val titleText = stringResource(R.string.rename_pair_title)
    val labelText = stringResource(R.string.host_pair_name_label)
    val confirmText = stringResource(R.string.btn_confirm)
    val cancelText = stringResource(R.string.cancel)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                titleText,
                modifier = Modifier.semantics { heading() }
            )
        },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text(labelText) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(newName) },
                modifier = Modifier.semantics { role = Role.Button }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.semantics { role = Role.Button }
            ) {
                Text(cancelText)
            }
        }
    )
}

@Composable
private fun KickConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val titleText = stringResource(R.string.kick_confirm_title)
    val messageText = stringResource(R.string.kick_confirm_message)
    val confirmText = stringResource(R.string.btn_confirm)
    val cancelText = stringResource(R.string.cancel)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                titleText,
                modifier = Modifier.semantics { heading() }
            )
        },
        text = { Text(messageText) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.semantics { role = Role.Button }
            ) { Text(confirmText) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.semantics { role = Role.Button }
            ) {
                Text(cancelText)
            }
        }
    )
}

@Composable
private fun EndSessionConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val titleText = stringResource(R.string.end_session_confirm_title)
    val messageText = stringResource(R.string.end_session_confirm_message)
    val confirmText = stringResource(R.string.btn_confirm)
    val cancelText = stringResource(R.string.cancel)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                titleText,
                modifier = Modifier.semantics { heading() }
            )
        },
        text = { Text(messageText) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.semantics { role = Role.Button }
            ) { Text(confirmText) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.semantics { role = Role.Button }
            ) {
                Text(cancelText)
            }
        }
    )
}

@Composable
private fun PairActiveContent(
    state: PairActiveUiState.Content,
    onRename: () -> Unit,
    onKick: () -> Unit,
    onEndSession: () -> Unit,
    onLeave: () -> Unit
) {
    val trophyDesc = stringResource(R.string.cd_trophy)
    val partnerConnectedText = stringResource(R.string.partner_connected)
    val pairNameLabel = stringResource(R.string.pair_name_label)
    val partnerLabel = stringResource(R.string.partner_label)
    val renameDesc = stringResource(R.string.rename)
    val endSessionText = stringResource(R.string.host_pair_end_session)
    val kickPartnerText = stringResource(R.string.kick_partner)
    val leavePairText = stringResource(R.string.leave_pair)

    val cardDescription = buildString {
        append(pairNameLabel)
        append(": ")
        append(state.pairName)
        if (state.partnerId != null) {
            append(", ")
            append(partnerLabel)
            append(": ")
            append("${state.partnerId.take(8)}...")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Default.EmojiEvents,
            contentDescription = trophyDesc,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            partnerConnectedText,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() }
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = cardDescription
                }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            pairNameLabel,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(state.pairName, style = MaterialTheme.typography.titleLarge)
                    }
                    if (state.isCreator) {
                        IconButton(onClick = onRename) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = renameDesc
                            )
                        }
                    }
                }

                if (state.partnerId != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        partnerLabel,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        "${state.partnerId.take(8)}...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        if (state.isCreator) {
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onEndSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { role = Role.Button },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(endSessionText)
            }

            if (state.partnerId != null) {
                OutlinedButton(
                    onClick = onKick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { role = Role.Button }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(kickPartnerText)
                }
            }
        } else {
            Spacer(Modifier.weight(1f))

            OutlinedButton(
                onClick = onLeave,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { role = Role.Button }
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(leavePairText)
            }
        }
    }
}
