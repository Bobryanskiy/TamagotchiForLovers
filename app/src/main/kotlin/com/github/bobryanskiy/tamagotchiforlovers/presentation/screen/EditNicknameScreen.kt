package com.github.bobryanskiy.tamagotchiforlovers.presentation.screen

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.remember
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
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.EditNicknameUiState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.EditNicknameViewModel
import com.github.bobryanskiy.tamagotchiforlovers.util.ValidationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNicknameScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditNicknameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nickname by viewModel.nickname.collectAsStateWithLifecycle()
    val isLoading = uiState is EditNicknameUiState.Loading

    val nameErrorResId = remember(nickname) {
        if (nickname.isNotEmpty()) ValidationUtils.getNicknameErrorResId(nickname) else null
    }

    val nameError = nameErrorResId?.let { stringResource(it) }
    val charCountDesc = stringResource(R.string.character_count, nickname.length, NameLimits.NICKNAME_MAX)
    val savingDesc = stringResource(R.string.saving)
    val titleText = stringResource(R.string.edit_nickname_title)
    val backDesc = stringResource(R.string.back)
    val labelText = stringResource(R.string.nickname_label)
    val savingText = stringResource(R.string.saving)
    val saveButtonText = stringResource(R.string.btn_save)

    LaunchedEffect(uiState) {
        if (uiState is EditNicknameUiState.Success) onNavigateBack()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = nickname,
                onValueChange = viewModel::onNicknameChange,
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
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                        )
                    } else {
                        Text(
                            "${nickname.length} / ${NameLimits.NICKNAME_MAX}",
                            modifier = Modifier.semantics {
                                contentDescription = charCountDesc
                            }
                        )
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = viewModel::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        role = Role.Button
                        if (isLoading) {
                            stateDescription = savingDesc
                        }
                    },
                enabled = nameErrorResId == null && !isLoading
            ) {
                if (isLoading) {
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
}
