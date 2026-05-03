package com.github.bobryanskiy.tamagotchiforlovers.ui.screens.petMain

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Основной экран с питомцем.
 * Отображает питомца и 4 кнопки характеристик снизу.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetMainScreen(
    petId: Int,
    onNavigateToMiniGames: (String) -> Unit,
    onBackToHome: () -> Unit
) {
    // Заглушка данных для демонстрации
    val petName = "Барбос"
    val petLevel = 5
    val stats = listOf(
        StatItem("Сила", 10, StatType.STRENGTH),
        StatItem("Ловкость", 8, StatType.AGILITY),
        StatItem("Интеллект", 12, StatType.INTELLIGENCE),
        StatItem("Здоровье", 15, StatType.HEALTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(petName) },
                navigationIcon = {
                    IconButton(onClick = onBackToHome) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "На главную"
                        )
                    }
                },
                actions = {
                    // Кнопки для онлайн-функций
                    IconButton(onClick = { /* Вывод в онлайн */ }) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Выйти в онлайн"
                        )
                    }
                    IconButton(onClick = { /* Генерация кода */ }) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Код подключения"
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
        ) {
            // Область отображения питомца
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Изображение питомца (заглушка)
                    Surface(
                        modifier = Modifier.size(200.dp),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "🐕",
                                fontSize = 100.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = petName,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    AssistChip(
                        onClick = { },
                        label = { Text("Уровень $petLevel") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            Divider()

            // Сетка с характеристиками (4 кнопки)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Характеристики",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.height(280.dp)
                    ) {
                        items(stats.size) { index ->
                            val stat = stats[index]
                            StatCard(
                                stat = stat,
                                onClick = { onNavigateToMiniGames(stat.type.name) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Элемент характеристики
 */
data class StatItem(
    val name: String,
    val value: Int,
    val type: StatType
)

enum class StatType {
    STRENGTH,     // Сила
    AGILITY,      // Ловкость
    INTELLIGENCE, // Интеллект
    HEALTH        // Здоровье
}

/**
 * Карточка характеристики
 */
@Composable
private fun StatCard(
    stat: StatItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stat.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stat.value.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Нажми для тренировки",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
