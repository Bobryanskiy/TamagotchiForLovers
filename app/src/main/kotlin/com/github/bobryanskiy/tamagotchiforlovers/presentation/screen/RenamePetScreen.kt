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
import androidx.compose.material3.CircularProgressIndicator
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
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.RenamePetUiState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.RenamePetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenamePetScreen(
    petId: String,
    onNavigateBack: () -> Unit,
    onRenamed: () -> Unit,
    viewModel: RenamePetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val newName by viewModel.newName.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is RenamePetUiState.Success) onRenamed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.rename_pet_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is RenamePetUiState.Loading -> {
                androidx.compose.foundation.layout.Box(
                    Modifier.fillMaxSize().padding(padding),
                    Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is RenamePetUiState.Loaded, is RenamePetUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        stringResource(R.string.rename_pet_hint),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(24.dp))

                    OutlinedTextField(
                        value = newName,
                        onValueChange = viewModel::onNameChange,
                        label = { Text(stringResource(R.string.pet_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = uiState is RenamePetUiState.Error
                    )

                    if (uiState is RenamePetUiState.Error) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource((uiState as RenamePetUiState.Error).messageResId),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = viewModel::rename,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = newName.isNotBlank()
                    ) {
                        Text(stringResource(R.string.btn_save))
                    }
                }
            }
            is RenamePetUiState.Error -> {
                androidx.compose.foundation.layout.Box(
                    Modifier.fillMaxSize().padding(padding),
                    Alignment.Center
                ) {
                    Text(
                        stringResource(state.messageResId),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}