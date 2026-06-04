package com.github.bobryanskiy.tamagotchiforlovers.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.model.Pet
import com.github.bobryanskiy.tamagotchiforlovers.presentation.navigation.AppRoute
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.AuthEvent
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.AuthUiState
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.AuthViewModel
import com.github.bobryanskiy.tamagotchiforlovers.util.ValidationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isSignUpMode by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val haptic = LocalHapticFeedback.current

    val emailErrorResId = remember(email) {
        if (email.isNotEmpty()) ValidationUtils.getEmailErrorResId(email) else null
    }
    val passwordErrorResId = remember(password) {
        if (password.isNotEmpty()) ValidationUtils.getPasswordErrorResId(password) else null
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.Error -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                val message = resources.getString(state.messageResId)
                snackbarHostState.showSnackbar(message)
            }
            is AuthUiState.Success -> {}
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is AuthEvent.NavigateToBoot -> {
                    navController.navigate(AppRoute.Boot) {
                        popUpTo<AppRoute.Auth> { inclusive = true }
                        launchSingleTop = true
                    }
                }
                is AuthEvent.ShowError -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (isSignUpMode) R.string.auth_title_register
                            else R.string.auth_title_login
                        ),
                        modifier = Modifier.semantics { heading() }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        AuthContent(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp),
            email = email,
            password = password,
            passwordVisible = passwordVisible,
            isSignUpMode = isSignUpMode,
            isLoading = uiState is AuthUiState.Loading,
            emailErrorResId = emailErrorResId,
            passwordErrorResId = passwordErrorResId,
            onEmailChange = { email = it },
            onPasswordChange = { password = it },
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
            onToggleMode = { isSignUpMode = !isSignUpMode },
            onSubmit = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (isSignUpMode) viewModel.register(email, password)
                else viewModel.login(email, password)
            }
        )
    }

    if (uiState is AuthUiState.Conflict) {
        val conflict = uiState as AuthUiState.Conflict
        ConflictResolutionDialog(
            localPet = conflict.localPet,
            remotePet = conflict.remotePet,
            onChooseLocal = { viewModel.resolveConflict(chooseLocal = true) },
            onChooseRemote = { viewModel.resolveConflict(chooseLocal = false) }
        )
    }
}

@Composable
private fun AuthContent(
    modifier: Modifier = Modifier,
    email: String,
    password: String,
    passwordVisible: Boolean,
    isSignUpMode: Boolean,
    isLoading: Boolean,
    emailErrorResId: Int?,
    passwordErrorResId: Int?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onToggleMode: () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit
) {
    val isFormValid = emailErrorResId == null
            && passwordErrorResId == null
            && email.isNotBlank()
            && password.isNotBlank()

    val emailError = emailErrorResId?.let { stringResource(it) }
    val passwordError = passwordErrorResId?.let { stringResource(it) }
    val loadingDescription = if (isLoading) stringResource(R.string.loading) else null
    val titleText = stringResource(
        if (isSignUpMode) R.string.auth_title_register
        else R.string.auth_title_login
    )
    val emailLabel = stringResource(R.string.email)
    val passwordLabel = stringResource(R.string.password)
    val hidePasswordDesc = stringResource(R.string.hide_password)
    val showPasswordDesc = stringResource(R.string.show_password)
    val loadingText = stringResource(R.string.loading)
    val submitText = stringResource(if (isSignUpMode) R.string.register else R.string.login)
    val toggleText = if (isSignUpMode) {
        stringResource(R.string.have_account) + " " + stringResource(R.string.login)
    } else {
        stringResource(R.string.no_account) + " " + stringResource(R.string.auth_title_register)
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = titleText,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text(emailLabel) },
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    emailError?.let { error(it) }
                },
            enabled = !isLoading,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = emailErrorResId != null,
            supportingText = if (emailErrorResId != null) {
                {
                    Text(
                        stringResource(emailErrorResId),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    )
                }
            } else null
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(passwordLabel) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    passwordError?.let { error(it) }
                },
            enabled = !isLoading,
            singleLine = true,
            isError = passwordErrorResId != null,
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                val description = if (passwordVisible) hidePasswordDesc else showPasswordDesc
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(imageVector = image, contentDescription = description)
                }
            },
            supportingText = if (passwordErrorResId != null) {
                {
                    Text(
                        stringResource(passwordErrorResId),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    )
                }
            } else null
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    role = Role.Button
                    loadingDescription?.let { stateDescription = it }
                },
            enabled = !isLoading && isFormValid
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.height(8.dp))
                Text(loadingText)
            } else {
                Text(submitText)
            }
        }

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = onToggleMode,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { role = Role.Button },
            enabled = !isLoading
        ) {
            Text(toggleText)
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ConflictResolutionDialog(
    localPet: Pet,
    remotePet: Pet,
    onChooseLocal: () -> Unit,
    onChooseRemote: () -> Unit
) {
    val dateFormat = remember {
        java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
    }

    AlertDialog(
        onDismissRequest = { /* нельзя закрыть без выбора */ },
        title = { Text(stringResource(R.string.conflict_title)) },
        text = {
            Column {
                Text(stringResource(R.string.conflict_message))
                Spacer(Modifier.height(16.dp))

                Text(
                    stringResource(R.string.conflict_local_version),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text("• ${stringResource(R.string.pet_name)}: ${localPet.profile.name}")
                Text("• ${stringResource(R.string.last_update)}: ${dateFormat.format(java.util.Date(localPet.stats.updatedAt))}")
                Text("• ${stringResource(R.string.stat_hunger)}: ${localPet.stats.hunger}%")

                Spacer(Modifier.height(12.dp))

                Text(
                    stringResource(R.string.conflict_remote_version),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text("• ${stringResource(R.string.pet_name)}: ${remotePet.profile.name}")
                Text("• ${stringResource(R.string.last_update)}: ${dateFormat.format(java.util.Date(remotePet.stats.updatedAt))}")
                Text("• ${stringResource(R.string.stat_hunger)}: ${remotePet.stats.hunger}%")
            }
        },
        confirmButton = {
            Button(onClick = onChooseLocal) {
                Text(stringResource(R.string.conflict_keep_local))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onChooseRemote) {
                Text(stringResource(R.string.conflict_use_cloud))
            }
        }
    )
}
