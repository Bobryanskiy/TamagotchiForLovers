package com.github.bobryanskiy.tamagotchiforlovers.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StartScreen(
    hasActiveSession: Boolean,
    onNewGame: () -> Unit,
    onContinue: () -> Unit,
    onJoinGame: () -> Unit,
    onAccountClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onAccountClick) {
                    Text("👤", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🥚 Виртуальный Питомец", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(24.dp))

            if (hasActiveSession) {
                Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                    Text("Продолжить игру")
                }
                Spacer(Modifier.height(12.dp))
            }

            Button(onClick = onNewGame, modifier = Modifier.fillMaxWidth()) {
                Text("Создать новую игру")
            }
            
            Spacer(Modifier.height(12.dp))
            
            OutlinedButton(onClick = onJoinGame, modifier = Modifier.fillMaxWidth()) {
                Text("Присоединиться к игре")
            }
        }
    }
}