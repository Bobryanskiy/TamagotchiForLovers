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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.PairActiveUiState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.PairActiveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairActiveScreen(
    pairId: String,
    onNavigateBack: () -> Unit,
    onSessionEnded: () -> Unit,
    viewModel: PairActiveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRenameDialog by remember { mutableStateOf(false) }
    var showKickDialog by remember { mutableStateOf(false) }
    var showEndDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is PairActiveUiState.Ended) onSessionEnded()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pair_active_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is PairActiveUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator()
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
                        CircularProgressIndicator()
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
        val content = uiState as PairActiveUiState.Content
        var newName by remember { mutableStateOf(content.pairName) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text(stringResource(R.string.rename_pair_title)) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text(stringResource(R.string.host_pair_name_label)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.renamePair(newName)
                        showRenameDialog = false
                    }
                ) { Text(stringResource(R.string.btn_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showKickDialog) {
        AlertDialog(
            onDismissRequest = { showKickDialog = false },
            title = { Text(stringResource(R.string.kick_confirm_title)) },
            text = { Text(stringResource(R.string.kick_confirm_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.kickPartner()
                        showKickDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.btn_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showKickDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showEndDialog) {
        AlertDialog(
            onDismissRequest = { showEndDialog = false },
            title = { Text(stringResource(R.string.end_session_confirm_title)) },
            text = { Text(stringResource(R.string.end_session_confirm_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.endSession()
                        showEndDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.btn_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showEndDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun PairActiveContent(
    state: PairActiveUiState.Content,
    onRename: () -> Unit,
    onKick: () -> Unit,
    onEndSession: () -> Unit,
    onLeave: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Default.EmojiEvents,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            stringResource(R.string.partner_connected),
            style = MaterialTheme.typography.headlineMedium
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.pair_name_label), style = MaterialTheme.typography.labelMedium)
                        Text(state.pairName, style = MaterialTheme.typography.titleLarge)
                    }
                    if (state.isCreator) {
                        IconButton(onClick = onRename) {
                            Icon(Icons.Default.Edit, stringResource(R.string.rename))
                        }
                    }
                }

                if (state.partnerId != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.partner_label),
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
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Stop, null)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.host_pair_end_session))
            }

            if (state.partnerId != null) {
                OutlinedButton(
                    onClick = onKick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.kick_partner))
                }
            }
        } else {
            Spacer(Modifier.weight(1f))

            OutlinedButton(
                onClick = onLeave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.leave_pair))
            }
        }
    }
}