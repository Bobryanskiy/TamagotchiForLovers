package com.github.bobryanskiy.tamagotchiforlovers.presentation.screen

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.ProfileUiState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditNickname: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    val titleText = stringResource(R.string.menu_profile)
    val backDesc = stringResource(R.string.back)
    val loadingDesc = stringResource(R.string.loading_profile)
    val logoutTitle = stringResource(R.string.confirm_logout_title)
    val logoutMessage = stringResource(R.string.confirm_logout_message)
    val confirmLogoutText = stringResource(R.string.btn_confirm_logout)
    val cancelText = stringResource(R.string.cancel)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        titleText,
                        modifier = Modifier.semantics { heading() }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, backDesc)
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics {
                            contentDescription = loadingDesc
                        }
                    )
                }
            }
            is ProfileUiState.Content -> {
                ProfileContent(
                    modifier = Modifier.padding(padding),
                    email = state.email,
                    isGuest = state.isGuest,
                    pet = state.activePet,
                    onRequestLogout = { showLogoutDialog = true },
                    onRequestLogin = onNavigateToLogin,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToEditNickname = onNavigateToEditNickname
                )
            }
            is ProfileUiState.Error -> {
                ErrorPlaceholder(
                    modifier = Modifier.padding(padding),
                    messageResId = state.messageResId,
                    onRetry = { viewModel.loadProfile() }
                )
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    logoutTitle,
                    modifier = Modifier.semantics { heading() }
                )
            },
            text = { Text(logoutMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logout()
                        showLogoutDialog = false
                        onNavigateToLogin()
                    },
                    modifier = Modifier.semantics { role = Role.Button }
                ) {
                    Text(confirmLogoutText)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    modifier = Modifier.semantics { role = Role.Button }
                ) {
                    Text(cancelText)
                }
            }
        )
    }
}

@Composable
private fun ProfileContent(
    modifier: Modifier = Modifier,
    email: String?,
    isGuest: Boolean,
    pet: Pet?,
    onRequestLogout: () -> Unit,
    onRequestLogin: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditNickname: () -> Unit
) {
    val ownerLabel = stringResource(R.string.profile_owner_label)
    val guestUser = stringResource(R.string.guest_user)
    val unknownUser = stringResource(R.string.unknown_user)
    val activePetLabel = stringResource(R.string.profile_active_pet_label)
    val statsLabel = stringResource(R.string.profile_pet_stats_label)
    val hungerLabel = stringResource(R.string.stat_hunger)
    val energyLabel = stringResource(R.string.stat_energy)
    val cleanlinessLabel = stringResource(R.string.stat_cleanliness)
    val happinessLabel = stringResource(R.string.stat_happiness)
    val noPetText = stringResource(R.string.profile_no_pet)
    val editNicknameText = stringResource(R.string.edit_nickname)
    val settingsText = stringResource(R.string.settings_title)
    val linkAccountText = stringResource(R.string.btn_link_account)
    val logoutText = stringResource(R.string.btn_logout)

    val ownerDescription = buildString {
        append(ownerLabel)
        append(": ")
        append(
            when {
                isGuest -> guestUser
                email != null -> email
                else -> unknownUser
            }
        )
    }

    val petDescription = pet?.let {
        buildString {
            append(activePetLabel)
            append(": ")
            append(it.profile.name)
            append(". ")
            append(hungerLabel); append(": "); append("${it.stats.hunger}%. ")
            append(energyLabel); append(": "); append("${it.stats.energy}%. ")
            append(cleanlinessLabel); append(": "); append("${it.stats.cleanliness}%. ")
            append(happinessLabel); append(": "); append("${it.stats.happiness}%")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = ownerDescription
                }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = ownerLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when {
                        isGuest -> guestUser
                        email != null -> email
                        else -> unknownUser
                    },
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        if (pet != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {
                        petDescription?.let { contentDescription = it }
                    }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = activePetLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() }
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(text = pet.profile.name, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = statsLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() }
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatItem(hungerLabel, "${pet.stats.hunger}%")
                        StatItem(energyLabel, "${pet.stats.energy}%")
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatItem(cleanlinessLabel, "${pet.stats.cleanliness}%")
                        StatItem(happinessLabel, "${pet.stats.happiness}%")
                    }
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = noPetText,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (!isGuest) {
            OutlinedButton(
                onClick = onNavigateToEditNickname,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { role = Role.Button }
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(editNicknameText)
            }
        }

        OutlinedButton(
            onClick = onNavigateToSettings,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { role = Role.Button }
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(settingsText)
        }

        Spacer(Modifier.weight(1f))

        if (isGuest) {
            Button(
                onClick = onRequestLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { role = Role.Button }
            ) {
                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(linkAccountText)
            }
        } else {
            Button(
                onClick = onRequestLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { role = Role.Button },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(logoutText)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    val itemDescription = "$label: $value"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = itemDescription
        }
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorPlaceholder(
    modifier: Modifier = Modifier,
    messageResId: Int,
    onRetry: () -> Unit
) {
    val errorDesc = stringResource(R.string.error_occurred)
    val errorTitle = stringResource(R.string.profile_error_title)
    val errorMessage = stringResource(messageResId)
    val refreshText = stringResource(R.string.profile_btn_refresh)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = errorDesc,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = errorTitle,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.semantics { role = Role.Button }
        ) {
            Text(refreshText)
        }
    }
}
