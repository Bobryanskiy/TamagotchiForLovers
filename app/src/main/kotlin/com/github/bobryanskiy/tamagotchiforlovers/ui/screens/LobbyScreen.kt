package com.github.bobryanskiy.tamagotchiforlovers.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pair

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    pair: Pair?,
    onAccept: (String) -> Unit,
    onLeave: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Лобби") }) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (pair == null) {
                CircularProgressIndicator()
            } else {
                Text("Код приглашения: ${pair.inviteKey?.code ?: "Генерация..."}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                Text(pair.status.name, style = MaterialTheme.typography.bodyLarge)

                Spacer(Modifier.height(24.dp))
                if (pair.pendingRequest != null) {
                    Button(onClick = { onAccept(pair.pendingRequest.guestId) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Принять игрока")
                    }
                } else {
                    Text("Ожидание второго игрока...", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onLeave) { Text("Покинуть лобби") }
            }
        }
    }
}