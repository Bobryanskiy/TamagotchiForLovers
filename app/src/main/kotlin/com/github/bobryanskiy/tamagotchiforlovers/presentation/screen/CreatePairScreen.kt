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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    viewModel: CreatePairViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pairName by viewModel.pairName.collectAsStateWithLifecycle()

    val nameErrorResId = remember(pairName) {
        if (pairName.isNotEmpty()) ValidationUtils.getPairNameErrorResId(pairName) else null
    }

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
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.host_pair_name_hint), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = pairName,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.host_pair_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nameErrorResId != null,
                supportingText = {
                    if (nameErrorResId != null) {
                        Text(stringResource(nameErrorResId), color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("${pairName.length} / ${NameLimits.PAIR_NAME_MAX}")
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = viewModel::createPair,
                modifier = Modifier.fillMaxWidth(),
                enabled = nameErrorResId == null && uiState !is CreatePairUiState.Loading
            ) {
                Text(stringResource(R.string.host_pair_generate_key))
            }
        }
    }
}