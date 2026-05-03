package com.example.petgame.ui.screens.miniGames

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Экран мини-игр для улучшения характеристик.
 * Заглушка для будущей реализации.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniGamesScreen(
    statType: String,
    onGameComplete: (Int) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Тренировка: ${getStatName(statType)}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.SportsBasketball,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Мини-игры в разработке",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Здесь будет игра для улучшения характеристики \"${getStatName(statType)}\"",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Планируемые мини-игры:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    listOf(
                        "• Математические задачки для Интеллекта",
                        "• Рефлекторные игры для Ловкости",
                        "• Силовые испытания для Силы",
                        "• Викторины о здоровье для Здоровья"
                    ).forEach { item ->
                        Text(
                            text = item,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onGameComplete(5) }, // Заглушка: +5 к характеристике
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Демо: +5 к характеристике", fontSize = 18.sp)
            }
        }
    }
}

private fun getStatName(statType: String): String {
    return when (statType) {
        "STRENGTH" -> "Сила"
        "AGILITY" -> "Ловкость"
        "INTELLIGENCE" -> "Интеллект"
        "HEALTH" -> "Здоровье"
        else -> statType
    }
}
