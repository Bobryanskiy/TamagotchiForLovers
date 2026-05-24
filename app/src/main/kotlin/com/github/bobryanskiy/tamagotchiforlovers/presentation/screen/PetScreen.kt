package com.github.bobryanskiy.tamagotchiforlovers.presentation.screen

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bobryanskiy.tamagotchiforlovers.BuildConfig
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.CalculateLiveStatsUseCase
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.MathTaskGeneratorUseCase
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.PairButtonState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.PetUiState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.PetViewModel
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.UiEvent
import kotlinx.coroutines.delay
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetScreen(
    petId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCreatePair: (String) -> Unit,
    onNavigateToPairWaiting: (String) -> Unit,
    onNavigateToPairActive: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMain: () -> Unit,
    onRenamePet: (String) -> Unit,
    viewModel: PetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pairState by viewModel.pairButtonState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }
    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:${context.packageName}".toUri()
                }
                context.startActivity(intent)
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(10_000L)
            currentTimeMs = System.currentTimeMillis()
        }
    }

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
                        text = (uiState as? PetUiState.Content)?.pet?.profile?.name
                            ?: stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    PairButton(
                        state = pairState,
                        petId = petId,
                        onCreatePair = onNavigateToCreatePair,
                        onOpenWaiting = onNavigateToPairWaiting,
                        onOpenActive = onNavigateToPairActive
                    )

                    IconButton(onClick = { onNavigateToProfile() }) {
                        Icon(Icons.Default.Person, stringResource(R.string.menu_profile))
                    }

                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, stringResource(R.string.pet_cd_settings))
                    }

                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        if (BuildConfig.DEBUG) {
                            DropdownMenuItem(
                                text = { Text("🔔 Test notification", color = MaterialTheme.colorScheme.tertiary) },
                                onClick = {
                                    showMenu = false
                                    viewModel.testNotification()
                                },
                                leadingIcon = { Icon(Icons.Default.Notifications, null) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_rename)) },
                            onClick = {
                                showMenu = false
                                onRenamePet(petId)
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.menu_delete),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                viewModel.abandonPet(onDeleted = onNavigateToMain)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is PetUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
            is PetUiState.Content -> {
                val livePet = remember(state.pet, currentTimeMs) {
                    CalculateLiveStatsUseCase()(state.pet, currentTimeMs)
                }
                PetContent(
                    modifier = Modifier.padding(padding),
                    pet = livePet,
                    onActionRequested = viewModel::onActionRequested
                )
            }
            is PetUiState.GameOver -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.game_over_title),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onNavigateToMain) {
                            Text(stringResource(R.string.game_over_button))
                        }
                    }
                }
            }
        }
    }

    dialogState?.let { state ->
        MathTaskDialog(
            action = state.action,
            isProcessing = state.isProcessing,
            onDismiss = viewModel::onDialogDismiss,
            onTaskCompleted = viewModel::onTaskCompleted,
            taskGenerator = viewModel.taskGenerator
        )
    }
}

@Composable
private fun PairButton(
    state: PairButtonState,
    petId: String,
    onCreatePair: (String) -> Unit,
    onOpenWaiting: (String) -> Unit,
    onOpenActive: (String) -> Unit
) {
    when (state) {
        is PairButtonState.Loading -> {}
        is PairButtonState.NoPair -> {
            IconButton(onClick = { onCreatePair(petId) }) {
                Icon(Icons.Default.Link, stringResource(R.string.pet_cd_create_pair))
            }
        }
        is PairButtonState.WaitingApproval -> {
            IconButton(onClick = { onOpenWaiting(state.pairId) }) {
                if (state.hasPendingRequests) {
                    BadgedBox(badge = { Badge { Text("!") } }) {
                        Icon(Icons.Default.Link, stringResource(R.string.pet_cd_waiting_pair))
                    }
                } else {
                    Icon(Icons.Default.Link, stringResource(R.string.pet_cd_waiting_pair))
                }
            }
        }
        is PairButtonState.Active -> {
            IconButton(onClick = { onOpenActive(state.pairId) }) {
                Icon(Icons.Default.Favorite, stringResource(R.string.pet_cd_active_pair))
            }
        }
    }
}

@Composable
private fun PetContent(
    modifier: Modifier = Modifier,
    pet: Pet,
    onActionRequested: (PetAction) -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }
    var lastUpdateAt by remember { mutableLongStateOf(pet.stats.updatedAt) }

    LaunchedEffect(pet.stats.updatedAt) {
        if (pet.stats.updatedAt != lastUpdateAt) {
            lastUpdateAt = pet.stats.updatedAt
            isAnimating = true
            delay(1500)
            isAnimating = false
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Image(
                painter = painterResource(
                    id = if (isAnimating) R.drawable.ic_hse_bird_action
                    else R.drawable.ic_hse_bird_idle
                ),
                contentDescription = stringResource(R.string.pet_cd_mascot),
                modifier = Modifier.size(200.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )

            Spacer(Modifier.height(16.dp))

            StatCard(stringResource(R.string.stat_hunger), pet.stats.hunger, "🍖")
            Spacer(Modifier.height(8.dp))
            StatCard(stringResource(R.string.stat_energy), pet.stats.energy, "⚡")
            Spacer(Modifier.height(8.dp))
            StatCard(stringResource(R.string.stat_cleanliness), pet.stats.cleanliness, "✨")
            Spacer(Modifier.height(8.dp))
            StatCard(stringResource(R.string.stat_happiness), pet.stats.happiness, "😄")
        }

        item {
            Text(stringResource(R.string.actions_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ActionBtn(PetAction.Feed, "🍖", stringResource(R.string.action_feed), onActionRequested)
                ActionBtn(PetAction.Play, "🎮", stringResource(R.string.action_play), onActionRequested)
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
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
                if (isError) Text(
                    stringResource(R.string.task_error),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (userAnswer.toIntOrNull() == currentTask.correctAnswer) onTaskCompleted()
                    else isError = true
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
private fun getActionText(action: PetAction): String = when (action) {
    PetAction.Feed -> stringResource(R.string.action_feed).lowercase()
    PetAction.Play -> stringResource(R.string.action_play).lowercase()
    PetAction.Clean -> stringResource(R.string.action_clean).lowercase()
    PetAction.Rest -> stringResource(R.string.action_rest).lowercase()
}

@Composable
private fun StatCard(label: String, value: Int, icon: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = MaterialTheme.typography.titleLarge.fontSize)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelMedium)
                LinearProgressIndicator(
                    progress = { value / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = when {
                        value > 60 -> MaterialTheme.colorScheme.primary
                        value > 30 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
            Spacer(Modifier.width(16.dp))
            Text("$value%", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActionBtn(
    action: PetAction,
    icon: String,
    label: String,
    onClick: (PetAction) -> Unit
) {
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