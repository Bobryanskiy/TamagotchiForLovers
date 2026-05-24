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

    val nameErrorResId = remember(nickname) {
        if (nickname.isNotEmpty()) ValidationUtils.getNicknameErrorResId(nickname) else null
    }

    LaunchedEffect(uiState) {
        if (uiState is EditNicknameUiState.Success) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_nickname_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = nickname,
                onValueChange = viewModel::onNicknameChange,
                label = { Text(stringResource(R.string.nickname_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nameErrorResId != null,
                supportingText = {
                    if (nameErrorResId != null) {
                        Text(stringResource(nameErrorResId), color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("${nickname.length} / ${NameLimits.NICKNAME_MAX}")
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
                enabled = nameErrorResId == null && uiState !is EditNicknameUiState.Loading
            ) {
                if (uiState is EditNicknameUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.btn_save))
                }
            }
        }
    }
}