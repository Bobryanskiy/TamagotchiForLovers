package com.github.bobryanskiy.tamagotchiforlovers.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.MathTaskGeneratorUseCase
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.PetUiState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.PetViewModel
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.TaskDialogState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.UiEvent
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetScreen(
    petId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCreatePair: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToMain: () -> Unit, // ✅ Для корректного выхода после удаления
    viewModel: PetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Обработка ошибок (Toast/Snackbar)
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            if (event is UiEvent.ShowError) {
                val message = context.applicationContext.getString(event.messageResId)
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = (uiState as? PetUiState.Content)?.pet?.profile?.name ?: stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    // ✅ Кнопка создания пары
                    IconButton(onClick = { onNavigateToCreatePair(petId) }) {
                        Icon(Icons.Default.Link, contentDescription = "Create Pair")
                    }

                    // ✅ Кнопка Профиля
                    IconButton(onClick = { onNavigateToProfile(petId) }) {
                        Icon(Icons.Default.Person, contentDescription = stringResource(R.string.menu_profile))
                    }

                    // ✅ Меню настроек
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Settings")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_rename)) },
                            onClick = {
                                showMenu = false
                                // TODO: Логика переименования
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_delete), color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                viewModel.abandonPet(onDeleted = onNavigateToMain)
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is PetUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is PetUiState.Content -> {
                PetContent(
                    modifier = Modifier.padding(padding),
                    pet = state.pet,
                    onActionRequested = viewModel::onActionRequested
                )
            }
            is PetUiState.GameOver -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.game_over_title), style = MaterialTheme.typography.headlineMedium)
                        Button(onClick = onNavigateToMain) {
                            Text(stringResource(R.string.game_over_button))
                        }
                    }
                }
            }
        }
    }

    // ✅ Диалог с математической задачей
    dialogState?.let { dialogState ->
        MathTaskDialog(
            action = dialogState.action,
            isProcessing = dialogState.isProcessing,
            onDismiss = viewModel::onDialogDismiss,
            onTaskCompleted = viewModel::onTaskCompleted,
            taskGenerator = viewModel.taskGenerator // Берем из ViewModel
        )
    }
}

// --- ВСПОМОГАТЕЛЬНЫЕ COMPOSABLES ---

@Composable
private fun PetContent(
    modifier: Modifier = Modifier,
    pet: com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet,
    onActionRequested: (PetAction) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            // Маскот (Ворон)
            Image(
                painter = painterResource(id = R.drawable.ic_pet_icon), // Замени на своего ворона
                contentDescription = "Mascot",
                modifier = Modifier.size(200.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Статы
            StatCard(label = stringResource(R.string.stat_hunger), value = pet.stats.hunger, icon = "🍖")
            StatCard(label = stringResource(R.string.stat_energy), value = pet.stats.energy, icon = "⚡")
            StatCard(label = stringResource(R.string.stat_cleanliness), value = pet.stats.cleanliness, icon = "✨")
            StatCard(label = stringResource(R.string.stat_happiness), value = pet.stats.happiness, icon = "😄")
        }

        item {
            Text(stringResource(R.string.actions_title), style = MaterialTheme.typography.titleMedium)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ActionBtn(PetAction.Feed, "🍖", stringResource(R.string.action_feed), onActionRequested)
                ActionBtn(PetAction.Play, "🎮", stringResource(R.string.action_play), onActionRequested)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ActionBtn(PetAction.Clean, "🧼", stringResource(R.string.action_clean), onActionRequested)
                ActionBtn(PetAction.Rest, "😴", stringResource(R.string.action_rest), onActionRequested)
            }
        }
    }
}

@Composable
private fun MathTaskDialog(
    action: PetAction,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onTaskCompleted: () -> Unit,
    taskGenerator: MathTaskGeneratorUseCase
) {
    val currentTask = remember { taskGenerator() }
    var userAnswer by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        title = { Text(stringResource(R.string.task_title)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.task_description, getActionText(action)),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(16.dp))
                Text(currentTask.question, style = MaterialTheme.typography.headlineMedium)

                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = userAnswer,
                    onValueChange = { userAnswer = it; isError = false },
                    label = { Text(stringResource(R.string.task_input_label)) },
                    isError = isError,
                    singleLine = true
                )
                if (isError) Text(stringResource(R.string.task_error), color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (userAnswer.toIntOrNull() == currentTask.correctAnswer) {
                        onTaskCompleted()
                    } else {
                        isError = true
                    }
                },
                enabled = !isProcessing && userAnswer.isNotBlank()
            ) {
                Text(stringResource(R.string.task_button_check))
            }
        },
        dismissButton = {
            if (!isProcessing) TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun StatCard(label: String, value: Int, icon: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = MaterialTheme.typography.titleLarge.fontSize)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium)
                LinearProgressIndicator(
                    progress = { value / 100f },
                    modifier = Modifier.width(150.dp),
                    color = when {
                        value > 60 -> MaterialTheme.colorScheme.primary
                        value > 30 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
            Spacer(Modifier.weight(1f))
            Text("$value%", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
    }
}

@Composable
private fun ActionBtn(action: PetAction, icon: String, label: String, onClick: (PetAction) -> Unit) {
    Button(
        onClick = { onClick(action) },
        modifier = Modifier.width(100.dp).height(80.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 24.sp)
            Text(label, fontSize = 12.sp)
        }
    }
}

@Composable
private fun getActionText(action: PetAction): String {
    return when (action) {
        PetAction.Feed -> stringResource(R.string.action_feed).lowercase()
        PetAction.Play -> stringResource(R.string.action_play).lowercase()
        PetAction.Clean -> stringResource(R.string.action_clean).lowercase()
        PetAction.Rest -> stringResource(R.string.action_rest).lowercase()
    }
}