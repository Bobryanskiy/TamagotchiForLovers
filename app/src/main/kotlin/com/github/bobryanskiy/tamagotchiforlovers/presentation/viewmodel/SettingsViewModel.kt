package com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bobryanskiy.tamagotchiforlovers.core.locale.LanguageManager
import com.github.bobryanskiy.tamagotchiforlovers.core.locale.LanguageOption
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val currentLanguageCode: String,
    val supportedLanguages: List<LanguageOption>,
    val notificationsEnabled: Boolean,
    val soundEnabled: Boolean,
    val appVersion: String,
    val isLanguageDialogOpen: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val languageManager: LanguageManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _currentLanguage = MutableStateFlow(languageManager.getCurrentLanguage())
    private val _isLanguageDialogOpen = MutableStateFlow(false)

    // Комбинируем Flow из разных источников
    val uiState: StateFlow<SettingsUiState> = combine(
        _currentLanguage,
        _isLanguageDialogOpen,
        settingsRepository.observeNotificationsEnabled(),
        settingsRepository.observeSoundEnabled()
    ) { language, dialogOpen, notifications, sound ->
        SettingsUiState(
            currentLanguageCode = language,
            supportedLanguages = languageManager.getSupportedLanguages(),
            notificationsEnabled = notifications,
            soundEnabled = sound,
            appVersion = settingsRepository.getAppVersion(),
            isLanguageDialogOpen = dialogOpen
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(
            currentLanguageCode = languageManager.getCurrentLanguage(),
            supportedLanguages = languageManager.getSupportedLanguages(),
            notificationsEnabled = true,
            soundEnabled = true,
            appVersion = settingsRepository.getAppVersion(),
            isLanguageDialogOpen = false
        )
    )

    fun openLanguageDialog() {
        _isLanguageDialogOpen.value = true
    }

    fun closeLanguageDialog() {
        _isLanguageDialogOpen.value = false
    }

    fun selectLanguage(code: String) {
        languageManager.setLanguage(code)
        _currentLanguage.value = code
        _isLanguageDialogOpen.value = false
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
            // UI обновится автоматически через Flow!
        }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSoundEnabled(enabled)
            // UI обновится автоматически через Flow!
        }
    }
}