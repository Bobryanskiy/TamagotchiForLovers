package com.github.bobryanskiy.tamagotchiforlovers.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.CreatePetUiState
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.CreatePetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePetScreen(
    viewModel: CreatePetViewModel = hiltViewModel(),
    navController: NavHostController,
    onNavigateToPet: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CreatePetUiState.Success -> {
                onNavigateToPet(state.petId)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_pet)) }
            )
        }
    ) { padding ->
        CreatePetContent(
            modifier = Modifier.padding(padding).padding(24.dp),
            petName = viewModel.petName,
            onNameChange = viewModel::onNameChange,
            onCreateClick = viewModel::createPet,
            isLoading = uiState is CreatePetUiState.Loading,
            error = when (uiState) {
                is CreatePetUiState.Error -> (uiState as CreatePetUiState.Error).message
                else -> null
            }
        )
    }
}

@Composable
private fun CreatePetContent(
    modifier: Modifier = Modifier,
    petName: String,
    onNameChange: (String) -> Unit,
    onCreateClick: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.create_pet_description),
            style = MaterialTheme.typography.bodyLarge
        )

        OutlinedTextField(
            value = petName,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.pet_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = error != null
        )

        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = onCreateClick,
            modifier = Modifier.fillMaxWidth(0.7f),
            enabled = !isLoading && petName.isNotBlank()
        ) {
            Text(
                text = if (isLoading) {
                    stringResource(R.string.creating_pet)
                } else {
                    stringResource(R.string.create_pet_button)
                }
            )
        }
    }
}