package com.github.bobryanskiy.tamagotchiforlovers.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.CreatePairUiState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.CreatePairViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePairScreen(
    petId: String,
    onNavigateBack: () -> Unit,
    onPairCreated: (String) -> Unit,
    viewModel: CreatePairViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pairName by viewModel.pairName.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is CreatePairUiState.Success) {
            onPairCreated((uiState as CreatePairUiState.Success).pairId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_pair_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                stringResource(R.string.host_pair_name_hint),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = pairName,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.host_pair_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState is CreatePairUiState.Error
            )

            if (uiState is CreatePairUiState.Error) {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource((uiState as CreatePairUiState.Error).messageResId),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = viewModel::createPair,
                modifier = Modifier.fillMaxWidth(),
                enabled = pairName.isNotBlank() && uiState !is CreatePairUiState.Loading
            ) {
                Text(stringResource(R.string.host_pair_generate_key))
            }
        }
    }
}