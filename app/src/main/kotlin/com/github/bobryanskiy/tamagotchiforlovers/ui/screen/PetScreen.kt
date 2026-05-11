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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleanHands
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FoodBank
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetStats
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.PetUiState
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.PetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetScreen(
    viewModel: PetViewModel = hiltViewModel(),
    navController: NavHostController,
    petId: String,
    onNavigateBack: () -> Unit,
    onNavigateToPuzzle: (String, PetActionType) -> Unit,
    onNavigateToCreatePair: (String) -> Unit,
    onNavigateToJoinRequests: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_pet)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToCreatePair(petId) }) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = stringResource(R.string.create_pair)
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
                        onNavigateToPuzzle(state.pet.id, action)
                    },
                    onCreatePair = { 
                        onNavigateToCreatePair(state.pet.id)
                    },
                    onShowJoinRequests = {
                        val pairId = state.pet.profile.currentPairId
                        if (pairId != null) {
                            onNavigateToJoinRequests(pairId)
                        }
                    }
                )
            }
            is PetUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = { /* Retry logic */ }
                )
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
                    contentDescription = stringResource(R.string.pet),
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
            label = stringResource(R.string.hunger),
            value = stats.hunger,
            onClick = { onActionClick(PetActionType.Feed) }
        )

        StatActionButton(
            icon = Icons.Default.Power,
            label = stringResource(R.string.energy),
            value = stats.energy,
            onClick = { onActionClick(PetActionType.Rest) }
        )

        StatActionButton(
            icon = Icons.Default.CleanHands,
            label = stringResource(R.string.cleanliness),
            value = stats.cleanliness,
            onClick = { onActionClick(PetActionType.Clean) }
        )

        StatActionButton(
            icon = Icons.Default.Favorite,
            label = stringResource(R.string.happiness),
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
                Text(stringResource(R.string.create_pair))
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
                Text(stringResource(R.string.pair_requests))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.error_state),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.try_again))
        }
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
                        progress = { value / 100f },
                        modifier = Modifier.fillMaxWidth(0.5f).height(6.dp),
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
