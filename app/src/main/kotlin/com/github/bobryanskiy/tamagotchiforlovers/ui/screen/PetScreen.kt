package com.github.bobryanskiy.tamagotchiforlovers.ui.screen

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CleanHands
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FoodBank
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Sleep
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetStats
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.PetUiState
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.PetViewModel

@Composable
fun PetScreen(
    viewModel: PetViewModel = hiltViewModel(),
    navController: NavHostController,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мой питомец") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Create pair */ }) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = "Создать пару"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is PetUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            is PetUiState.Content -> {
                PetContent(
                    modifier = Modifier.padding(padding),
                    petName = state.pet.profile.name,
                    stats = state.pet.stats,
                    onActionClick = { action ->
                        viewModel.onActionSelected(action)
                    },
                    onCreatePair = { /* TODO: Navigate to create pair */ },
                    onShowJoinRequests = { /* TODO: Show join requests */ }
                )
            }
            is PetUiState.Error -> {
                // TODO: Show error state
            }
        }
    }
}

@Composable
private fun PetContent(
    modifier: Modifier = Modifier,
    petName: String,
    stats: PetStats,
    onActionClick: (PetActionType) -> Unit,
    onCreatePair: () -> Unit,
    onShowJoinRequests: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pet display area
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = "Питомец",
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = petName,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Stats section with 4 buttons
        StatActionButton(
            icon = Icons.Default.FoodBank,
            label = "Голод",
            value = stats.hunger,
            onClick = { onActionClick(PetActionType.Feed) }
        )

        StatActionButton(
            icon = Icons.Default.Sleep,
            label = "Энергия",
            value = stats.energy,
            onClick = { onActionClick(PetActionType.Rest) }
        )

        StatActionButton(
            icon = Icons.Default.CleanHands,
            label = "Чистота",
            value = stats.cleanliness,
            onClick = { onActionClick(PetActionType.Clean) }
        )

        StatActionButton(
            icon = Icons.Default.Favorite,
            label = "Счастье",
            value = stats.happiness,
            onClick = { onActionClick(PetActionType.Play) }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Pair management buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCreatePair,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text("Создать пару")
            }

            Button(
                onClick = onShowJoinRequests,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Power,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text("Запросы")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatActionButton(
    icon: ImageVector,
    label: String,
    value: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    LinearProgressIndicator(
                        progress = value / 100f,
                        modifier = Modifier.fillMaxWidth(150.dp).height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.3f)
                    )
                }
            }
            Text(
                text = "$value%",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

sealed class PetActionType {
    object Feed : PetActionType()
    object Rest : PetActionType()
    object Clean : PetActionType()
    object Play : PetActionType()
}
