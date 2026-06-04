package com.github.bobryanskiy.tamagotchiforlovers.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
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

    var isSaving by remember { mutableStateOf(false) }

    val nameError = nameErrorResId?.let { stringResource(it) }
    val charCountDesc = stringResource(R.string.character_count, newName.length, NameLimits.PET_NAME_MAX)
    val savingDesc = stringResource(R.string.saving)
    val titleText = stringResource(R.string.rename_pet_title)
    val backDesc = stringResource(R.string.back)
    val loadingDesc = stringResource(R.string.loading)
    val hintText = stringResource(R.string.rename_pet_hint)
    val labelText = stringResource(R.string.pet_name)
    val saveButtonText = stringResource(R.string.btn_save)
    val savingText = stringResource(R.string.saving)

    LaunchedEffect(uiState) {
        when (uiState) {
            is RenamePetUiState.Success -> {
                isSaving = false
                onRenamed()
            }
            is RenamePetUiState.Error -> {
                isSaving = false
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        titleText,
                        modifier = Modifier.semantics { heading() }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, backDesc)
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is RenamePetUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics {
                            contentDescription = loadingDesc
                        }
                    )
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
                    Text(hintText, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(24.dp))

                    OutlinedTextField(
                        value = newName,
                        onValueChange = viewModel::onNameChange,
                        label = { Text(labelText) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                nameError?.let { error(it) }
                            },
                        enabled = !isSaving,
                        singleLine = true,
                        isError = nameErrorResId != null,
                        supportingText = {
                            if (nameErrorResId != null) {
                                Text(
                                    stringResource(nameErrorResId),
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                                )
                            } else {
                                Text(
                                    "${newName.length} / ${NameLimits.PET_NAME_MAX}",
                                    modifier = Modifier.semantics {
                                        contentDescription = charCountDesc
                                    }
                                )
                            }
                        }
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            isSaving = true
                            viewModel.rename()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                role = Role.Button
                                if (isSaving) {
                                    stateDescription = savingDesc
                                }
                            },
                        enabled = nameErrorResId == null
                                && newName.isNotBlank()
                                && !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(savingText)
                        } else {
                            Text(saveButtonText)
                        }
                    }
                }
            }

            is RenamePetUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        stringResource(state.messageResId),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = newName,
                        onValueChange = viewModel::onNameChange,
                        label = { Text(labelText) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                nameError?.let { error(it) }
                            },
                        singleLine = true,
                        isError = nameErrorResId != null,
                        supportingText = {
                            if (nameErrorResId != null) {
                                Text(
                                    stringResource(nameErrorResId),
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text("${newName.length} / ${NameLimits.PET_NAME_MAX}")
                            }
                        }
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            isSaving = true
                            viewModel.rename()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { role = Role.Button },
                        enabled = nameErrorResId == null && newName.isNotBlank()
                    ) {
                        Text(saveButtonText)
                    }
                }
            }
        }
    }
}
