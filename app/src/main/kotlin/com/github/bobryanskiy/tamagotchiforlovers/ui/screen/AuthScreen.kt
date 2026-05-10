package com.github.bobryanskiy.tamagotchiforlovers.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.AuthUiState
import com.github.bobryanskiy.tamagotchiforlovers.ui.viewmodel.AuthViewModel
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
    var isSignUpMode by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Валидация полей
    val emailError = if (email.isNotEmpty()) ValidationUtils.getEmailError(email) else null
    val passwordError = if (password.isNotEmpty()) ValidationUtils.getPasswordError(password) else null

    // Обработка состояний аутентификации
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            is AuthUiState.Success -> {
                // Успешная авторизация - возвращаемся назад
                navController.popBackStack()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isSignUpMode) "Создание аккаунта" else "Вход в аккаунт") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
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
            isSignUpMode = isSignUpMode,
            isLoading = uiState is AuthUiState.Loading,
            emailError = emailError,
            passwordError = passwordError,
            onEmailChange = { email = it },
            onPasswordChange = { password = it },
            onToggleMode = { isSignUpMode = !isSignUpMode },
            onSubmit = {
                if (isSignUpMode) {
                    viewModel.register(email, password)
                } else {
                    viewModel.login(email, password)
                }
            }
        )
    }
}

@Composable
private fun AuthContent(
    modifier: Modifier = Modifier,
    email: String,
    password: String,
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
            text = if (isSignUpMode) "Создать аккаунт" else "Вход в аккаунт",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true,
            isError = emailError != null,
            supportingText = if (emailError != null) {{ Text(emailError) }} else null
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true,
            isError = passwordError != null,
            supportingText = if (passwordError != null) {{ Text(passwordError) }} else null
        )

        Spacer(modifier = Modifier.height(24.dp))

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
                Text(if (isSignUpMode) "Создать" else "Войти")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onToggleMode,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isSignUpMode) "Уже есть аккаунт? Войти" else "Нет аккаунта? Создать")
        }
    }
}
