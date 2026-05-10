package com.github.bobryanskiy.tamagotchiforlovers.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetTask
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.PuzzleUiState
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.PuzzleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleScreen(
    viewModel: PuzzleViewModel = hiltViewModel(),
    navController: NavHostController,
    petId: String,
    actionType: PetActionType,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var userAnswer by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController  .current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Задание для питомца") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is PuzzleUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            is PuzzleUiState.Content -> {
                PuzzleContent(
                    modifier = Modifier.padding(padding),
                    task = state.task,
                    userAnswer = userAnswer,
                    onAnswerChange = { userAnswer = it },
                    onSubmit = {
                        viewModel.checkAnswer(userAnswer)
                        keyboard?.hide()
                    },
                    focusRequester = focusRequester
                )
            }
            is PuzzleUiState.Success -> {
                LaunchedEffect(Unit) {
                    onNavigateBack()
                }
            }
            is PuzzleUiState.Error -> {
                PuzzleContent(
                    modifier = Modifier.padding(padding),
                    task = state.task,
                    userAnswer = userAnswer,
                    onAnswerChange = { userAnswer = it },
                    onSubmit = {
                        viewModel.checkAnswer(userAnswer)
                        keyboard?.hide()
                    },
                    errorMessage = state.message,
                    focusRequester = focusRequester
                )
            }
        }
    }
}

@Composable
private fun PuzzleContent(
    modifier: Modifier = Modifier,
    task: PetTask,
    userAnswer: String,
    onAnswerChange: (String) -> Unit,
    onSubmit: () -> Unit,
    errorMessage: String? = null,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = task.example.question,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (task.example.hint.isNotEmpty()) {
                            Text(
                                text = "💡 Подсказка: ${task.example.hint}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = userAnswer,
            onValueChange = onAnswerChange,
            label = { Text("Ваш ответ") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            isError = errorMessage != null
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = userAnswer.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("Проверить ответ")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
