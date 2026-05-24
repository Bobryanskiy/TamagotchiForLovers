package com.github.bobryanskiy.tamagotchiforlovers.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.NameLimits
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.RenamePetUiState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.RenamePetViewModel
import com.github.bobryanskiy.tamagotchiforlovers.util.ValidationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenamePetScreen(
    onNavigateBack: () -> Unit,
    onRenamed: () -> Unit,
    viewModel: RenamePetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val newName by viewModel.newName.collectAsStateWithLifecycle()

    val nameErrorResId = remember(newName) {
        if (newName.isNotEmpty()) ValidationUtils.getPetNameErrorResId(newName) else null
    }

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
        when (uiState) {
            is RenamePetUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is RenamePetUiState.Loaded, is RenamePetUiState.Success -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(stringResource(R.string.rename_pet_hint), style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(24.dp))

                    OutlinedTextField(
                        value = newName,
                        onValueChange = viewModel::onNameChange,
                        label = { Text(stringResource(R.string.pet_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = nameErrorResId != null,
                        supportingText = {
                            if (nameErrorResId != null) {
                                Text(stringResource(nameErrorResId), color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("${newName.length} / ${NameLimits.PET_NAME_MAX}")
                            }
                        }
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = viewModel::rename,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = nameErrorResId == null && newName.isNotBlank()
                    ) {
                        Text(stringResource(R.string.btn_save))
                    }
                }
            }
            is RenamePetUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Text(
                        stringResource((uiState as RenamePetUiState.Error).messageResId),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}