package com.github.bobryanskiy.tamagotchiforlovers.ui.screens.connection

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Экран подключения к онлайн-игре.
 * Позволяет создать новую онлайн-игру или подключиться по коду.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionScreen(
    onConnectionSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var connectionCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showCreateGameDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Подключение к игре") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
                text = "Онлайн-игра",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Кнопка создания новой игры
            Button(
                onClick = { showCreateGameDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Создать новую игру", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Divider()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Или подключитесь по коду",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = connectionCode,
                onValueChange = { connectionCode = it },
                label = { Text("Код комнаты") },
                placeholder = { Text("XXXXXX") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                maxLines = 1
            )

            Button(
                onClick = {
                    isLoading = true
                    // TODO: Вызвать ViewModel для подключения по коду
                    // viewModel.connectByCode(connectionCode)
                    // После успеха: onConnectionSuccess()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = connectionCode.length >= 6 && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Подключиться", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Как это работает?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "1. Создайте игру и получите код\n2. Поделитесь кодом с другом\n3. Друг вводит код и подключается",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Диалог создания игры
    if (showCreateGameDialog) {
        CreateGameDialog(
            onConfirm = {
                // TODO: Создать игру через ViewModel
                showCreateGameDialog = false
                // После успеха: onConnectionSuccess()
            },
            onDismiss = { showCreateGameDialog = false }
        )
    }
}

/**
 * Диалог создания новой игры
 */
@Composable
private fun CreateGameDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Создание игры") },
        text = { Text("Вы хотите создать новую онлайн-игру? Ваш друг сможет подключиться по коду.") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
