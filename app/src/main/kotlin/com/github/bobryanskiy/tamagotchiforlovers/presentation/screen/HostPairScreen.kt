package com.github.bobryanskiy.tamagotchiforlovers.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.HostPairUiState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.HostPairViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostPairScreen(
    viewModel: HostPairViewModel = hiltViewModel(),
    petId: String,
    onNavigateBack: () -> Unit,
    onPairReady: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboard = LocalClipboard.current

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        viewModel.initScreen()
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            currentTime = System.currentTimeMillis()
            delay(1000L)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is HostPairUiState.Connected) {
            onPairReady()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = when (uiState) {
                        is HostPairUiState.Idle -> stringResource(R.string.create_pair_title)
                        is HostPairUiState.Waiting -> stringResource(R.string.waiting_partner_title)
                        is HostPairUiState.Connected -> stringResource(R.string.pair_active_title)
                        else -> stringResource(R.string.create_pair_title)
                    }
                    Text(title)
                },
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
                is HostPairUiState.Idle -> {
                    IdleContent(onCreate = { name -> viewModel.createPair(name, petId) })
                }
                is HostPairUiState.Waiting -> {
                    WaitingContent(
                        state = state,
                        currentTime = currentTime,
                        onCopyCode = {
                            clipboard.nativeClipboard.setPrimaryClip(
                                android.content.ClipData.newPlainText("InviteCode", state.inviteCode)
                            )
                        },
                        onRegenerateCode = { viewModel.regenerateInviteCode() },
                        onAccept = viewModel::acceptRequest,
                        onReject = viewModel::rejectRequest
                    )
                }
                is HostPairUiState.Connected -> {
                    ConnectedContent(
                        pairName = state.pairName,
                        onRename = { newName -> viewModel.renamePair(newName) },
                        onKick = { viewModel.kickPartner() },
                        onEndSession = { viewModel.endSession() },
                        onLeave = {
                            viewModel.resetToIdle()
                            onNavigateBack()
                        }
                    )
                }
                is HostPairUiState.Error -> {
                    ErrorContent(
                        messageResId = state.messageResId,
                        onRetry = {
                            viewModel.resetToIdle()
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun IdleContent(onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Придумайте имя для вашей пары",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Имя пары") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { if (name.isNotBlank()) onCreate(name) },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank()
        ) {
            Text("Сгенерировать ключ")
        }
    }
}

@Composable
private fun WaitingContent(
    state: HostPairUiState.Waiting,
    currentTime: Long,
    onCopyCode: () -> Unit,
    onRegenerateCode: () -> Unit,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit
) {
    val isExpired = state.expiresAt == 0L || currentTime > state.expiresAt
    val timeLeft = state.expiresAt - currentTime

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(R.string.your_invite_key), color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.height(8.dp))

                if (isExpired) {
                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(text = stringResource(R.string.key_expired), color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onRegenerateCode) {
                        Text(stringResource(R.string.regenerate_code))
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = state.inviteCode, style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        IconButton(onClick = onCopyCode) {
                            Icon(Icons.Default.ContentCopy, stringResource(R.string.copy_code), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${stringResource(R.string.key_valid_for)} ${formatTime(timeLeft)}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(text = stringResource(R.string.join_requests_title), modifier = Modifier.align(Alignment.Start), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (isExpired) {
            Text(text = stringResource(R.string.key_expired_message), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        } else if (state.pendingRequests.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.no_requests), textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                items(state.pendingRequests) { request ->
                    RequestItem(guestId = request.guestId, onAccept = { onAccept(request.guestId) }, onReject = { onReject(request.guestId) })
                }
            }
        }
    }
}

@Composable
private fun RequestItem(
    guestId: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${stringResource(R.string.guest_label)} ${guestId.take(8)}...",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(R.string.wants_to_join),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onAccept) {
                    Icon(
                        Icons.Default.Check,
                        stringResource(R.string.accept),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onReject) {
                    Icon(
                        Icons.Default.Close,
                        stringResource(R.string.reject),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectedContent(
    pairName: String,
    onRename: (String) -> Unit,
    onKick: () -> Unit,
    onLeave: () -> Unit,
    onEndSession: () -> Unit
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
            text = stringResource(R.string.partner_connected),
            style = MaterialTheme.typography.headlineMedium
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pairName,
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = { /* TODO: показать диалог переименования */ }) {
                        Icon(Icons.Default.Edit, stringResource(R.string.rename))
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onEndSession,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Завершить сессию")
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onKick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.kick_partner))
                }
            }
        }

        Spacer(Modifier.weight(1f))

        OutlinedButton(
            onClick = onLeave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.finish_and_exit))
        }
    }
}

@Composable
private fun ErrorContent(
    messageResId: Int,
    onRetry: () -> Unit
) {
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
            Text(stringResource(R.string.try_again))
        }
    }
}

private fun formatTime(millis: Long): String {
    if (millis <= 0) return "00:00"
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
}