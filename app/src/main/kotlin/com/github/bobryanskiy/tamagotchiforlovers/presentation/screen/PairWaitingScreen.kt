package com.github.bobryanskiy.tamagotchiforlovers.presentation.screen

import android.content.ClipData
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.PairWaitingUiState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.PairWaitingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairWaitingScreen(
    onNavigateBack: () -> Unit,
    onPairActivated: () -> Unit,
    viewModel: PairWaitingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboard = LocalClipboard.current
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val nicknames by viewModel.nicknames.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        while (isActive) {
            currentTime = System.currentTimeMillis()
            delay(1_000L)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is PairWaitingUiState.Activated) onPairActivated()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.waiting_partner_title)) },
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
                is PairWaitingUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is PairWaitingUiState.Waiting -> {
                    PairWaitingContent(
                        state = state,
                        currentTime = currentTime,
                        onCopyCode = {
                            val clip = ClipData.newPlainText("InviteCode", state.inviteCode)
                            clipboard.nativeClipboard.setPrimaryClip(clip)
                        },
                        onRegenerateCode = viewModel::regenerateInviteCode,
                        onAccept = viewModel::acceptRequest,
                        nicknames = nicknames,
                        onReject = viewModel::rejectRequest
                    )
                }
                is PairWaitingUiState.Activated -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is PairWaitingUiState.Error -> {
                    ErrorContent(
                        messageResId = state.messageResId,
                        onRetry = onNavigateBack
                    )
                }
            }
        }
    }
}

@Composable
private fun PairWaitingContent(
    state: PairWaitingUiState.Waiting,
    currentTime: Long,
    nicknames: Map<String, String?>,
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
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.your_invite_key),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(8.dp))

                if (isExpired) {
                    Icon(
                        Icons.Default.Warning,
                        null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.key_expired),
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onRegenerateCode) {
                        Text(stringResource(R.string.regenerate_code))
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            state.inviteCode,
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        IconButton(onClick = onCopyCode) {
                            Icon(
                                Icons.Default.ContentCopy,
                                stringResource(R.string.copy_code),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${stringResource(R.string.key_valid_for)} ${formatTime(timeLeft)}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.join_requests_title),
            modifier = Modifier.align(Alignment.Start),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))

        when {
            isExpired -> {
                Text(
                    stringResource(R.string.key_expired_message),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            state.pendingRequests.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_requests), textAlign = TextAlign.Center)
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(state.pendingRequests, key = { it.guestId }) { request ->
                        RequestItem(
                            guestId = request.guestId,
                            nickname = nicknames[request.guestId],
                            onAccept = { onAccept(request.guestId) },
                            onReject = { onReject(request.guestId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestItem(
    guestId: String,
    nickname: String?,
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
                    text = nickname ?: "${guestId.take(8)}...",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (nickname != null) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    stringResource(R.string.wants_to_join),
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
internal fun ErrorContent(messageResId: Int, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(messageResId),
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
    val seconds = (millis / 1_000) % 60
    val minutes = (millis / (1_000 * 60)) % 60
    return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
}