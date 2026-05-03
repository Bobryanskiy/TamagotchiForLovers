package com.example.petgame.ui.screens.petCreation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Экран создания питомца.
 * Позволяет пользователю выбрать тип питомца и дать ему имя.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetCreationScreen(
    onPetCreated: (Int) -> Unit,
    onBack: () -> Unit
) {
    var petName by remember { mutableStateOf("") }
    var selectedPetType by remember { mutableStateOf(PetType.DOG) }
    var showError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создание питомца") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Создайте своего питомца",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Выбор типа питомца
            Text(
                text = "Выберите тип питомца:",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                PetType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedPetType == type,
                        onClick = { selectedPetType = type },
                        label = { Text(type.displayName) },
                        leadingIcon = if (selectedPetType == type) {
                            {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null
                    )
                }
            }

            // Ввод имени питомца
            OutlinedTextField(
                value = petName,
                onValueChange = { petName = it },
                label = { Text("Имя питомца") },
                placeholder = { Text("Введите имя...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                isError = showError
            )

            if (showError) {
                Text(
                    text = "Пожалуйста, введите имя питомца",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Кнопка создания
            Button(
                onClick = {
                    if (petName.isNotBlank()) {
                        // TODO: Вызвать ViewModel для сохранения питомца
                        // viewModel.createPet(name = petName, type = selectedPetType)
                        // После успешного создания: onPetCreated(petId)
                        
                        // Заглушка для демонстрации
                        val fakePetId = 1
                        onPetCreated(fakePetId)
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = petName.isNotBlank()
            ) {
                Text("Создать питомца", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

enum class PetType(val displayName: String) {
    DOG("Собака"),
    CAT("Кошка"),
    BIRD("Птица"),
    HAMSTER("Хомяк")
}
