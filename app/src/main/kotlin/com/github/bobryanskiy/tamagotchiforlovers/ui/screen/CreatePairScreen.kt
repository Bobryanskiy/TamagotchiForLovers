package com.github.bobryanskiy.tamagotchiforlovers.ui.screen

import android.content.ClipData
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.CreatePairUiState
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.CreatePairViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePairScreen(
    viewModel: CreatePairViewModel = hiltViewModel(),
    navController: NavHostController,
    petId: String,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboard.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.creating_pair)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val state = uiState) {
            is CreatePairUiState.Idle -> {
                CreatePairContent(
                    modifier = Modifier.padding(padding),
                    onCreatePair = { pairName ->
                        viewModel.createPair(petId, pairName)
                    }
                )
            }
            is CreatePairUiState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is CreatePairUiState.Success -> {
                PairCreatedContent(
                    modifier = Modifier.padding(padding),
                    inviteCode = state.inviteCode,
                    onCopyCode = {
                        clipboard.nativeClipboard.setPrimaryClip(
                            ClipData.newPlainText(
                                "Invite Code",
                                state.inviteCode
                            )
                        )
                    },
                    onDone = onNavigateBack
                )
            }
            is CreatePairUiState.Error -> {
                CreatePairContent(
                    modifier = Modifier.padding(padding),
                    onCreatePair = { pairName ->
                        viewModel.createPair(petId, pairName)
                    },
                    errorMessage = state.message
                )
            }
        }
    }
}

@Composable
private fun CreatePairContent(
    modifier: Modifier = Modifier,
    onCreatePair: (String) -> Unit,
    errorMessage: String? = null
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = stringResource(R.string.creating_pair),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = stringResource(R.string.pair_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        var pairName by remember { mutableStateOf("") }

        OutlinedTextField(
            value = pairName,
            onValueChange = { pairName = it },
            label = { Text(stringResource(R.string.pair_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage != null
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onCreatePair(pairName) },
            modifier = Modifier.fillMaxWidth(),
            enabled = pairName.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(stringResource(R.string.create_pair_and_get_code))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PairCreatedContent(
    modifier: Modifier = Modifier,
    inviteCode: String,
    onCopyCode: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )

                Text(
                    text = stringResource(R.string.pair_created),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )

                Text(
                    text = stringResource(R.string.invite_code),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inviteCode,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    IconButton(onClick = onCopyCode) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(R.string.copy)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.send_code_to_friend),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.done))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
