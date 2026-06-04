package com.github.bobryanskiy.tamagotchiforlovers.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bobryanskiy.tamagotchiforlovers.BuildConfig
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.DeathCause
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.PetAction
import com.github.bobryanskiy.tamagotchiforlovers.domain.usecase.MathTaskGeneratorUseCase
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.PairButtonState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.PetUiState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.PetViewModel
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.UiEvent
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetScreen(
    petId: String,
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
    val haptic = LocalHapticFeedback.current
    val resources = LocalResources.current

    val appName = stringResource(R.string.app_name)
    val menuProfileDesc = stringResource(R.string.menu_profile)
    val settingsDesc = stringResource(R.string.pet_cd_settings)
    val testNotifText = "🔔 Test notification"
    val renameMenuText = stringResource(R.string.menu_rename)
    val deleteMenuText = stringResource(R.string.menu_delete)
    val loadingPetDesc = stringResource(R.string.loading_pet_data)
    val gameOverTitle = stringResource(R.string.game_over_title)
    val gameOverButtonText = stringResource(R.string.game_over_button)
    val newRequestsDesc = stringResource(R.string.new_requests_available)
    val createPairDesc = stringResource(R.string.pet_cd_create_pair)
    val waitingPairDesc = stringResource(R.string.pet_cd_waiting_pair)
    val hasPendingDesc = stringResource(R.string.has_pending_requests)
    val activePairDesc = stringResource(R.string.pet_cd_active_pair)

    LaunchedEffect(viewModel) {
        viewModel.start()
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowError -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val message = resources.getString(event.messageResId)
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = (uiState as? PetUiState.Content)?.pet?.profile?.name ?: appName,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() }
                    )
                },
                actions = {
                    PairButton(
                        state = pairState,
                        petId = petId,
                        onCreatePair = onNavigateToCreatePair,
                        onOpenWaiting = onNavigateToPairWaiting,
                        onOpenActive = onNavigateToPairActive,
                        createPairDesc = createPairDesc,
                        waitingPairDesc = waitingPairDesc,
                        hasPendingDesc = hasPendingDesc,
                        activePairDesc = activePairDesc,
                        newRequestsDesc = newRequestsDesc
                    )

                    IconButton(onClick = { onNavigateToProfile() }) {
                        Icon(Icons.Default.Person, menuProfileDesc)
                    }

                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, settingsDesc)
                    }

                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        if (BuildConfig.DEBUG) {
                            DropdownMenuItem(
                                text = { Text(testNotifText, color = MaterialTheme.colorScheme.tertiary) },
                                onClick = {
                                    showMenu = false
                                    viewModel.triggerAlarmManually()
                                },
                                leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(renameMenuText) },
                            onClick = {
                                showMenu = false
                                onRenamePet(petId)
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    deleteMenuText,
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
                                    contentDescription = null,
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
                    CircularProgressIndicator(
                        modifier = Modifier.semantics {
                            contentDescription = loadingPetDesc
                        }
                    )
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
                val deathMessageResId = when (state.pet.lifeState.deathCause) {
                    DeathCause.HUNGER -> R.string.death_cause_hunger
                    DeathCause.EXHAUSTION -> R.string.death_cause_exhaustion
                    DeathCause.DISEASE -> R.string.death_cause_disease
                    DeathCause.ESCAPED -> R.string.death_cause_escaped
                    null -> R.string.death_cause_unknown
                }

                val deathMessage = stringResource(deathMessageResId, state.pet.profile.name)

                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.game_over_title),
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.semantics { heading() }
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = deathMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = onNavigateToMain,
                            modifier = Modifier.semantics { role = Role.Button }
                        ) {
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
    onOpenActive: (String) -> Unit,
    createPairDesc: String,
    waitingPairDesc: String,
    hasPendingDesc: String,
    activePairDesc: String,
    newRequestsDesc: String
) {
    when (state) {
        is PairButtonState.Loading -> {}
        is PairButtonState.NoPair -> {
            IconButton(onClick = { onCreatePair(petId) }) {
                Icon(Icons.Default.Link, createPairDesc)
            }
        }
        is PairButtonState.WaitingApproval -> {
            IconButton(onClick = { onOpenWaiting(state.pairId) }) {
                if (state.hasPendingRequests) {
                    BadgedBox(
                        badge = {
                            Badge {
                                Text(
                                    "!",
                                    modifier = Modifier.semantics {
                                        contentDescription = newRequestsDesc
                                    }
                                )
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Link,
                            waitingPairDesc,
                            modifier = Modifier.semantics {
                                stateDescription = hasPendingDesc
                            }
                        )
                    }
                } else {
                    Icon(Icons.Default.Link, waitingPairDesc)
                }
            }
        }
        is PairButtonState.Active -> {
            IconButton(onClick = { onOpenActive(state.pairId) }) {
                Icon(Icons.Default.Favorite, activePairDesc)
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
    var lastStats by remember { mutableStateOf(pet.stats) }

    val statsTitleText = stringResource(R.string.stats_section_title)
    val actionsTitleText = stringResource(R.string.actions_title)
    val hungerText = stringResource(R.string.stat_hunger)
    val energyText = stringResource(R.string.stat_energy)
    val cleanlinessText = stringResource(R.string.stat_cleanliness)
    val happinessText = stringResource(R.string.stat_happiness)
    val feedText = stringResource(R.string.action_feed)
    val playText = stringResource(R.string.action_play)
    val cleanText = stringResource(R.string.action_clean)
    val restText = stringResource(R.string.action_rest)

    val petStatusDescription = remember(pet, hungerText, energyText, cleanlinessText, happinessText) {
        buildString {
            append("${pet.profile.name}. ")
            append("$hungerText: ${pet.stats.hunger}%. ")
            append("$energyText: ${pet.stats.energy}%. ")
            append("$cleanlinessText: ${pet.stats.cleanliness}%. ")
            append("$happinessText: ${pet.stats.happiness}%")
        }
    }

    LaunchedEffect(pet.stats) {
        val prev = lastStats
        val current = pet.stats

        val statsImproved = current.hunger > prev.hunger
                || current.energy > prev.energy
                || current.cleanliness > prev.cleanliness
                || current.happiness > prev.happiness

        if (statsImproved) {
            isAnimating = true
            delay(1500)
            isAnimating = false
        }

        lastStats = current
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        val screenHeight = maxHeight
        val mascotSize = (screenHeight * 0.28f).coerceIn(120.dp, 220.dp)
        val spacing = 8.dp

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            Image(
                painter = painterResource(
                    id = if (isAnimating) R.drawable.ic_hse_bird_action
                    else R.drawable.ic_hse_bird_idle
                ),
                contentDescription = petStatusDescription,
                modifier = Modifier
                    .size(mascotSize)
                    .semantics {
                        role = Role.Image
                        contentDescription = petStatusDescription
                    },
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )

            Text(
                statsTitleText,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                StatCard(hungerText, pet.stats.hunger, "🍖")
                StatCard(energyText, pet.stats.energy, "⚡")
                StatCard(cleanlinessText, pet.stats.cleanliness, "✨")
                StatCard(happinessText, pet.stats.happiness, "😄")
            }

            Text(
                actionsTitleText,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(spacing * 0.5f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActionBtn(PetAction.Feed, "🍖", feedText, onActionRequested)
                    ActionBtn(PetAction.Play, "🎮", playText, onActionRequested)
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActionBtn(PetAction.Clean, "🧼", cleanText, onActionRequested)
                    ActionBtn(PetAction.Rest, "😴", restText, onActionRequested)
                }
            }

            Spacer(Modifier.weight(1f))
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

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val titleText = stringResource(R.string.task_title)
    val descriptionText = stringResource(R.string.task_description, getActionText(action))
    val labelText = stringResource(R.string.task_input_label)
    val errorText = stringResource(R.string.task_error)
    val checkingDesc = stringResource(R.string.checking_answer)
    val checkButtonText = stringResource(R.string.task_button_check)
    val cancelText = stringResource(R.string.cancel)

    val checkAnswer = {
        val answer = userAnswer.toIntOrNull()
        if (answer == currentTask.correctAnswer) {
            onTaskCompleted()
        } else {
            isError = true
        }
    }

    LaunchedEffect(Unit) {
        delay(200)
        focusRequester.requestFocus()
        keyboardController?.show()
    }


    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        title = {
            Text(
                titleText,
                modifier = Modifier.semantics { heading() }
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    descriptionText,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    currentTask.question,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics {
                        liveRegion = LiveRegionMode.Polite
                    }
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = userAnswer,
                    onValueChange = { userAnswer = it; isError = false },
                    label = { Text(labelText) },
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .semantics {
                        if (isError) {
                            error(errorText)
                        }
                    },
                    isError = isError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { checkAnswer() }
                    ),
                    supportingText = if (isError) {
                        {
                            Text(
                                errorText,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive }
                            )
                        }
                    } else null
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { checkAnswer() },
                modifier = Modifier.semantics {
                    role = Role.Button
                    if (isProcessing) {
                        stateDescription = checkingDesc
                    }
                },
                enabled = !isProcessing && userAnswer.isNotBlank()
            ) {
                Text(checkButtonText)
            }
        },
        dismissButton = {
            if (!isProcessing) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.semantics { role = Role.Button }
                ) {
                    Text(cancelText)
                }
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
    val cardDescription = "$label: $value%"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .focusable()
            .semantics(mergeDescendants = true) {
                contentDescription = cardDescription
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = value / 100f,
                    range = 0f..1f,
                    steps = 100
                )
                liveRegion = LiveRegionMode.Polite
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                icon,
                fontSize = 20.sp,
                modifier = Modifier.clearAndSetSemantics { }
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelMedium)
                LinearProgressIndicator(
                    progress = { value / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {
                            contentDescription = cardDescription
                            progressBarRangeInfo = ProgressBarRangeInfo(
                                current = value / 100f,
                                range = 0f..1f,
                                steps = 100
                            )
                        },
                    color = when {
                        value > 60 -> MaterialTheme.colorScheme.primary
                        value > 30 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "$value%",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ActionBtn(action: PetAction, icon: String, label: String, onClick: (PetAction) -> Unit) {
    Button(
        onClick = { onClick(action) },
        modifier = Modifier
            .width(110.dp)
            .height(70.dp)
            .semantics {
                role = Role.Button
                contentDescription = label
            },
        contentPadding = PaddingValues(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                icon,
                fontSize = 22.sp,
                modifier = Modifier.clearAndSetSemantics { }
            )
            Text(label, fontSize = 11.sp, maxLines = 1)
        }
    }
}
