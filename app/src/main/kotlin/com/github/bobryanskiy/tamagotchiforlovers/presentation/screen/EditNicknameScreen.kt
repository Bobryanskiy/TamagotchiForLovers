package com.github.bobryanskiy.tamagotchiforlovers.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.result.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// presentation/screen/EditNicknameScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNicknameScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditNicknameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nickname by viewModel.nickname.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is EditNicknameUiState.Success) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_nickname_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = nickname,
                onValueChange = viewModel::onNicknameChange,
                label = { Text(stringResource(R.string.nickname_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
                enabled = nickname.isNotBlank()
            ) {
                Text(stringResource(R.string.btn_save))
            }
        }
    }
}

@HiltViewModel
class EditNicknameViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<EditNicknameUiState>(EditNicknameUiState.Idle)
    val uiState: StateFlow<EditNicknameUiState> = _uiState.asStateFlow()

    private val _nickname = MutableStateFlow("")
    val nickname: StateFlow<String> = _nickname.asStateFlow()

    fun onNicknameChange(value: String) {
        _nickname.value = value.take(20) // макс 20 символов
    }

    fun save() {
        val uid = authRepository.getCurrentUserId() ?: return
        val name = _nickname.value.trim()
        if (name.isBlank()) return

        viewModelScope.launch {
            _uiState.value = EditNicknameUiState.Loading
            when (userRepository.updateNickname(uid, name)) {
                is DomainResult.Success -> _uiState.value = EditNicknameUiState.Success
                is DomainResult.Failure -> _uiState.value = EditNicknameUiState.Error
            }
        }
    }
}

sealed class EditNicknameUiState {
    data object Idle : EditNicknameUiState()
    data object Loading : EditNicknameUiState()
    data object Success : EditNicknameUiState()
    data object Error : EditNicknameUiState()
}