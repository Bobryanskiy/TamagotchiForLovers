package com.github.bobryanskiy.tamagotchiforlovers.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetCriticalStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    pet: Pet?,
    onAction: (PetAction) -> Unit,
    onAbandon: () -> Unit,
    onCreatePair: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pet?.profile?.name ?: "Питомец") },
                actions = {
                    IconButton(onClick = onAbandon) { Text("Удалить") }
                }
            )
        }
    ) { padding ->
        if (pet == null) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val statusText = when (pet.profile.criticalStatus) {
                PetCriticalStatus.NORMAL -> "✅ В порядке"
                PetCriticalStatus.SICK -> "🤒 Болеет (статы падают ×2)"
                PetCriticalStatus.COLLAPSED -> "😴 Спит (ждём восстановления)"
                PetCriticalStatus.ESCAPED, PetCriticalStatus.DEAD -> "💔 Потерян"
            }
            Text(
                statusText,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(16.dp))

            StatBar("🍖 Голод", pet.stats.hunger)
            StatBar("⚡ Энергия", pet.stats.energy)
            StatBar("🛁 Чистота", pet.stats.cleanliness)
            StatBar("🎈 Настроение", pet.stats.happiness)
            Spacer(Modifier.height(24.dp))

            val isBlocked = pet.profile.criticalStatus in listOf(
                PetCriticalStatus.COLLAPSED,
                PetCriticalStatus.DEAD,
                PetCriticalStatus.ESCAPED
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ActionButton("Покормить", PetAction.Feed, isBlocked, onAction, Modifier.weight(1f))
                ActionButton("Поиграть", PetAction.Play, isBlocked, onAction, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ActionButton("Помыть", PetAction.Clean, isBlocked, onAction, Modifier.weight(1f))
                ActionButton("Уложить", PetAction.Rest, isBlocked, onAction, Modifier.weight(1f))
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onCreatePair,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Создать пару")
            }
        }
    }
}

@Composable
fun StatBar(label: String, value: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row {
            Text(label)
            Spacer(Modifier.weight(1f))
            Text("$value%")
        }
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier.fillMaxWidth().height(8.dp)
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun ActionButton(
    text: String,
    action: PetAction,
    disabled: Boolean,
    onClick: (PetAction) -> Unit,
    modifier: Modifier = Modifier // 👈 Принимаем modifier
) {
    Button(
        onClick = { onClick(action) },
        enabled = !disabled,
        modifier = modifier.fillMaxWidth() // 👈 Применяем к кнопке
    ) {
        Text(text)
    }
}