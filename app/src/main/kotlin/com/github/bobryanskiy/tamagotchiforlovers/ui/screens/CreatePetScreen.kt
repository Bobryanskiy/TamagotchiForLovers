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
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.PairViewModel
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.PetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePetScreen(
    navController: NavController,
    viewModel: PairViewModel = hiltViewModel(),
    petViewModel: PetViewModel = hiltViewModel(),
    forPair: Boolean = false
) {
    var petName by remember { mutableStateOf("") }
    var pairName by remember { mutableStateOf("") }
    val currentPet by petViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is PairViewModel.UiEvent.NavigateToGame -> {
                    navController.navigate("game") {
                        popUpTo("create_pet") { inclusive = true }
                    }
                }
                is PairViewModel.UiEvent.NavigateToLobby -> {
                    navController.navigate("lobby") {
                        popUpTo("create_pet") { inclusive = true }
                    }
                }
                is PairViewModel.UiEvent.ShowError -> {
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
            if (!forPair || currentPet == null) {
                OutlinedTextField(
                    value = petName,
                    onValueChange = { petName = it },
                    label = { Text("Имя питомца") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Показываем имя текущего питомца
                Text("Питомец: ${currentPet?.profile?.name}", modifier = Modifier.fillMaxWidth())
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
                    if (forPair && currentPet != null) {
                        // Создаем пару для существующего питомца
                        if (pairName.isNotBlank()) {
                            viewModel.createPairForExistingPet(currentPet!!.id, pairName)
                        }
                    } else if (forPair && pairName.isNotBlank()) {
                        // Создаем нового питомца и пару
                        viewModel.createNewSession(petName, pairName)
                    } else if (petName.isNotBlank()) {
                        // Просто создаем питомца
                        viewModel.createPet(petName)
                    }
                },
                enabled = if (forPair && currentPet != null) {
                    pairName.isNotBlank()
                } else {
                    petName.isNotBlank() && (!forPair || pairName.isNotBlank())
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