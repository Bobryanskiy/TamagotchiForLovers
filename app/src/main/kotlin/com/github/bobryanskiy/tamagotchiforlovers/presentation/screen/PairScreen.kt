package com.github.bobryanskiy.tamagotchiforlovers.presentation.screen

import android.content.ClipData
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.PairUiState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.PairViewModel
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.PairManagementData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairScreen(
    petId: String,
    onNavigateToPet: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PairViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val managementData by viewModel.managementData.collectAsStateWithLifecycle()
    val clipboard = LocalClipboard.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(when(uiState) {
                        is PairUiState.CreateMode -> stringResource(R.string.create_pair)
                        is PairUiState.ManageMode -> "Моя Пара"
                        else -> "Пара"
                    })
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->

        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "PairScreenAnimation",
            modifier = Modifier.padding(padding)
        ) { targetState ->
            when (targetState) {
                is PairUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is PairUiState.CreateMode -> {
                    CreatePairContent(
                        onCreatePair = { name -> viewModel.createPair(petId, name) },
                        onError = { /* Handle error via Snackbar in parent */ }
                    )
                }

                is PairUiState.ManageMode -> {
                    PairManagementContent(
                        data = managementData,
                        onAccept = { guestId -> viewModel.acceptRequest(targetState.pairId, guestId) },
                        onReject = { guestId -> viewModel.rejectRequest(targetState.pairId, guestId) },
                        onCopyCode = { code ->
                            clipboard.nativeClipboard.setPrimaryClip(ClipData.newPlainText("Invite Code", code))
                        },
                        onGoToPet = onNavigateToPet
                    )
                }

                is PairUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(targetState.message, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { /* Retry logic or back */ }) { Text("Retry") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreatePairContent(
    onCreatePair: (String) -> Unit,
    onError: (String) -> Unit
) {
    var pairName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Icon(Icons.Default.Pets, null, Modifier.size(48.dp))
                Text("Создать пару", style = MaterialTheme.typography.headlineSmall)
                Text("Придумайте название для вашей пары питомцев")
            }
        }

        OutlinedTextField(
            value = pairName,
            onValueChange = { pairName = it },
            label = { Text("Название пары") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { if (pairName.isNotBlank()) onCreatePair(pairName) },
            modifier = Modifier.fillMaxWidth(),
            enabled = pairName.isNotBlank()
        ) {
            Text("Создать и получить код")
        }
    }
}

@Composable
private fun PairManagementContent(
    data: PairManagementData,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    onCopyCode: (String) -> Unit,
    onGoToPet: () -> Unit
) {
    if (data.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val pair = data.pair ?: run {
        Text("Ошибка загрузки данных пары")
        return
    }

    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Пара: ${pair.name}", style = MaterialTheme.typography.titleLarge)
                Text("Статус: ${pair.status}", style = MaterialTheme.typography.bodyMedium)


                pair.inviteKey?.code?.let { code ->
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(onClick = { onCopyCode(code) }) {
                            Icon(Icons.Default.ContentCopy, "Copy")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Запросы на вступление:", style = MaterialTheme.typography.titleMedium)

        if (data.requests.isEmpty()) {
            Text("Нет активных запросов", modifier = Modifier.padding(top = 8.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(data.requests) { request ->
                    RequestItem(
                        guestId = request.guestId,
                        onAccept = { onAccept(request.guestId) },
                        onReject = { onReject(request.guestId) }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = onGoToPet, modifier = Modifier.fillMaxWidth()) {
            Text("Перейти к питомцу")
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
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Гость ID: ${guestId.take(8)}...", style = MaterialTheme.typography.bodyLarge)
                Text("Хочет присоединиться", style = MaterialTheme.typography.bodySmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAccept) { Text("Принять") }
                OutlinedButton(onClick = onReject, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Отклонить")
                }
            }
        }
    }
}