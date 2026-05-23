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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.PairManagementUiState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.PairManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairManagementScreen(
    pairId: String,
    viewModel: PairManagementViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(pairId) {
        viewModel.observePairAndRequests(pairId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Управление парой") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is PairManagementUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is PairManagementUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
            is PairManagementUiState.Active -> {
                Column(Modifier.padding(padding).padding(16.dp)) {
                    // Информация о паре
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Пара: ${state.pair.name}", style = MaterialTheme.typography.titleLarge)
                            Text("Статус: ${state.pair.status}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Список запросов
                    Text("Запросы на вступление:", style = MaterialTheme.typography.titleMedium)

                    if (state.requests.isEmpty()) {
                        Text("Нет активных запросов", modifier = Modifier.padding(top = 8.dp))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.requests) { request ->
                                RequestItem(
                                    guestId = request.guestId,
                                    onAccept = { viewModel.acceptRequest(pairId, request.guestId) },
                                    onReject = { viewModel.rejectRequest(pairId, request.guestId) }
                                )
                            }
                        }
                    }
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
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Гость ID: ${guestId.take(8)}...", style = MaterialTheme.typography.bodyLarge)
                Text("Хочет присоединиться", style = MaterialTheme.typography.bodySmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAccept, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("Принять")
                }
                OutlinedButton(onClick = onReject, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Отклонить")
                }
            }
        }
    }
}