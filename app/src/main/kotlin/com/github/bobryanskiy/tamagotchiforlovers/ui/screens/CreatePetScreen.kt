package com.github.bobryanskiy.tamagotchiforlovers.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.bobryanskiy.tamagotchiforlovers.ui.state.CreatePetUiState
import com.github.bobryanskiy.tamagotchiforlovers.ui.state.UiEvent
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.LobbyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePetScreen(
    navController: NavController,
    viewModel: LobbyViewModel = hiltViewModel(),
    forPair: Boolean = false
) {
    var petName by remember { mutableStateOf("") }
    var pairName by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.NavigateToGame -> {
                    navController.navigate("game") {
                        popUpTo("create_pet") { inclusive = true }
                    }
                }
                is UiEvent.NavigateToLobby -> {
                    navController.navigate("lobby") {
                        popUpTo("create_pet") { inclusive = true }
                    }
                }
                is UiEvent.ShowError -> {
                    // Можно показать Snackbar или Toast
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (forPair) "Создание пары" else "Создание питомца") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp)) {
            // Если создаем пару для существующего питомца - не показываем поле имени питомца
            if (!forPair) {
                OutlinedTextField(
                    value = petName,
                    onValueChange = { petName = it },
                    label = { Text("Имя питомца") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (forPair) {
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = pairName,
                    onValueChange = { pairName = it },
                    label = { Text("Название пары") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (forPair && pairName.isNotBlank()) {
                        // Создаем пару для существующего питомца (нужно будет передать petId)
                        // viewModel.createPairForExistingPet(petId, pairName)
                    } else if (forPair && petName.isNotBlank() && pairName.isNotBlank()) {
                        // Создаем нового питомца и пару
                        viewModel.createNewSession(petName, pairName)
                    } else if (petName.isNotBlank()) {
                        // Просто создаем питомца (через отдельный UseCase или метод)
                    }
                },
                enabled = if (forPair) {
                    pairName.isNotBlank() && (!forPair || petName.isNotBlank())
                } else {
                    petName.isNotBlank()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (forPair) "Создать пару" else "Создать питомца")
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = {
                navController.popBackStack()
            }) { Text("Назад") }
        }
    }
}