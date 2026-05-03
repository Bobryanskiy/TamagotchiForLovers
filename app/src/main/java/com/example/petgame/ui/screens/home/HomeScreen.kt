package com.example.petgame.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Главный экран приложения.
 * Отображает кнопки для начала новой игры, продолжения, подключения и входа в аккаунт.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    hasActiveGame: Boolean = false,
    activePetId: Int? = null,
    onStartNewGame: () -> Unit,
    onContinueGame: (Int) -> Unit,
    onConnectToGame: () -> Unit,
    onLoginClick: () -> Unit,
    onExitApp: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pet Game") },
                actions = {
                    IconButton(onClick = onLoginClick) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Person,
                            contentDescription = "Войти в аккаунт"
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Заголовок
            Text(
                text = "Добро пожаловать!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Кнопки действий
            Spacer(modifier = Modifier.height(16.dp))

            if (hasActiveGame && activePetId != null) {
                // Если есть активная игра - показываем кнопку "Продолжить"
                Button(
                    onClick = { onContinueGame(activePetId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Продолжить игру", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onStartNewGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Новая игра", fontSize = 18.sp)
                }
            } else {
                // Если нет активной игры - показываем "Начать новую игру"
                Button(
                    onClick = onStartNewGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Начать новую игру", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onConnectToGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text("Подключиться к игре", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onExitApp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Выход", fontSize = 18.sp)
            }
        }
    }
}
