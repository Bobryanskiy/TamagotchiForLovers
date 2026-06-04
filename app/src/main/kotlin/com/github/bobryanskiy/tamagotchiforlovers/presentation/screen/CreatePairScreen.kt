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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.NameLimits
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.CreatePairUiState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.CreatePairViewModel
import com.github.bobryanskiy.tamagotchiforlovers.util.ValidationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePairScreen(
    onNavigateBack: () -> Unit,
    onPairCreated: (String) -> Unit,
    onNavigateToAuth: () -> Unit,
    viewModel: CreatePairViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pairName by viewModel.pairName.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current
    val resources = LocalResources.current

    val nameErrorResId = remember(pairName) {
        if (pairName.isNotEmpty()) ValidationUtils.getPairNameErrorResId(pairName) else null
    }

    var showAuthRequiredDialog by remember { mutableStateOf(false) }

    val nameError = nameErrorResId?.let { stringResource(it) }
    val charCountDesc = stringResource(R.string.character_count, pairName.length, NameLimits.PAIR_NAME_MAX)
    val titleText = stringResource(R.string.create_pair_title)
    val backDesc = stringResource(R.string.back)
    val hintText = stringResource(R.string.host_pair_name_hint)
    val labelText = stringResource(R.string.host_pair_name_label)
    val buttonText = stringResource(R.string.host_pair_generate_key)

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CreatePairUiState.Success -> onPairCreated(state.pairId)
            is CreatePairUiState.Error -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (state.messageResId == R.string.error_not_authenticated) {
                    showAuthRequiredDialog = true
                } else {
                    val message = resources.getString(state.messageResId)
                    snackbarHostState.showSnackbar(message)
                }
            }
            else -> {}
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                hintText,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = pairName,
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
                        Text(
                            "${pairName.length} / ${NameLimits.PAIR_NAME_MAX}",
                            modifier = Modifier.semantics {
                                contentDescription = charCountDesc
                            }
                        )
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = viewModel::createPair,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { role = Role.Button },
                enabled = nameErrorResId == null && uiState !is CreatePairUiState.Loading
            ) {
                Text(buttonText)
            }
        }
    }

    if (showAuthRequiredDialog) {
        AuthRequiredDialog(
            onGoToLogin = {
                showAuthRequiredDialog = false
                onNavigateToAuth()
            },
            onDismiss = {
                showAuthRequiredDialog = false
            }
        )
    }
}

@Composable
private fun AuthRequiredDialog(
    onGoToLogin: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.auth_required_title),
                modifier = Modifier.semantics { heading() }
            )
        },
        text = { Text(stringResource(R.string.error_not_authenticated)) },
        confirmButton = {
            Button(
                onClick = onGoToLogin,
                modifier = Modifier.semantics { role = Role.Button }
            ) {
                Text(stringResource(R.string.btn_go_to_login))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.semantics { role = Role.Button }
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
