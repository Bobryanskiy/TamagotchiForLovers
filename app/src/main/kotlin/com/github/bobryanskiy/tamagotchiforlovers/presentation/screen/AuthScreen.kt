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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.github.bobryanskiy.tamagotchiforlovers.R
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
    val context = LocalContext.current

    val emailError = if (email.isNotEmpty()) ValidationUtils.getEmailError(email) else null
    val passwordError = if (password.isNotEmpty()) ValidationUtils.getPasswordError(password) else null

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.Error -> {
                val message = context.applicationContext.getString(state.messageResId)
                snackbarHostState.showSnackbar(message)
            }
            is AuthUiState.Success -> {
            }
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is AuthEvent.NavigateToMain -> {
                    navController.popBackStack()
                }
                is AuthEvent.NavigateToPet -> {
                    navController.navigate(AppRoute.Pet(event.petId)) {
                        popUpTo<AppRoute.Auth> { inclusive = true }
                    }
                }
                is AuthEvent.ShowError -> {
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (isSignUpMode) R.string.auth_title_register
                            else R.string.auth_title_login
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        AuthContent(
            modifier = Modifier.padding(padding).padding(24.dp),
            email = email,
            password = password,
            passwordVisible = passwordVisible,
            isSignUpMode = isSignUpMode,
            isLoading = uiState is AuthUiState.Loading,
            emailError = emailError,
            passwordError = passwordError,
            onEmailChange = { email = it },
            onPasswordChange = { password = it },
            onToggleMode = { isSignUpMode = !isSignUpMode },
            onSubmit = {
                if (isSignUpMode) viewModel.register(email, password)
                else viewModel.login(email, password)
            }
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
    emailError: String?,
    passwordError: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit
) {
    val isFormValid = emailError == null && passwordError == null && email.isNotBlank() && password.isNotBlank()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSignUpMode) stringResource(R.string.auth_title_register)
            else stringResource(R.string.auth_title_login),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text(stringResource(R.string.email)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true,
            isError = emailError != null,
            supportingText = if (emailError != null) {
                { Text(emailError) }
            } else null
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(R.string.password)) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true,
            isError = passwordError != null,
            supportingText = if (passwordError != null) {
                { Text(passwordError) }
            } else null
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && isFormValid
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(stringResource(if (isSignUpMode) R.string.register else R.string.login))
            }
        }

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = onToggleMode,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(
                if (isSignUpMode) stringResource(R.string.have_account) + " " + stringResource(R.string.login)
                else stringResource(R.string.no_account) + " " + stringResource(R.string.auth_title_register)
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}