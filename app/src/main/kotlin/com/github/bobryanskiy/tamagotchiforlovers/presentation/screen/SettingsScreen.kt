package com.github.bobryanskiy.tamagotchiforlovers.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.bobryanskiy.tamagotchiforlovers.R
import com.github.bobryanskiy.tamagotchiforlovers.core.locale.LanguageOption
import com.github.bobryanskiy.tamagotchiforlovers.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val titleText = stringResource(R.string.settings_title)
    val backDesc = stringResource(R.string.back)
    val accountSection = stringResource(R.string.settings_section_account)
    val languageTitle = stringResource(R.string.settings_language)
    val languageSystem = stringResource(R.string.settings_language_system)
    val notificationsSection = stringResource(R.string.settings_section_notifications)
    val notificationsTitle = stringResource(R.string.settings_notifications)
    val notificationsDesc = stringResource(R.string.settings_notifications_desc)
    val soundTitle = stringResource(R.string.settings_sound)
    val soundDesc = stringResource(R.string.settings_sound_desc)
    val aboutSection = stringResource(R.string.settings_section_about)
    val versionTitle = stringResource(R.string.settings_version)

    val currentLanguageName = state.supportedLanguages
        .find { it.code == state.currentLanguageCode }?.displayName
        ?: languageSystem

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionHeader(accountSection)

            SettingsClickItem(
                icon = Icons.Default.Language,
                title = languageTitle,
                subtitle = currentLanguageName,
                onClick = viewModel::openLanguageDialog
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsSectionHeader(notificationsSection)

            SettingsSwitchItem(
                icon = Icons.Default.Notifications,
                title = notificationsTitle,
                subtitle = notificationsDesc,
                checked = state.notificationsEnabled,
                onCheckedChange = viewModel::toggleNotifications
            )

            SettingsSwitchItem(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = soundTitle,
                subtitle = soundDesc,
                checked = state.soundEnabled,
                onCheckedChange = viewModel::toggleSound,
                enabled = state.notificationsEnabled
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsSectionHeader(aboutSection)

            SettingsClickItem(
                icon = Icons.Default.Info,
                title = versionTitle,
                subtitle = state.appVersion,
                onClick = { }
            )
        }
    }

    if (state.isLanguageDialogOpen) {
        LanguagePickerDialog(
            currentCode = state.currentLanguageCode,
            languages = state.supportedLanguages,
            onDismiss = viewModel::closeLanguageDialog,
            onSelect = viewModel::selectLanguage
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
            .semantics { heading() }
    )
}

@Composable
private fun SettingsClickItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .semantics {
                role = Role.Button
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    val switchOnState = stringResource(R.string.switch_state_on)
    val switchOffState = stringResource(R.string.switch_state_off)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .semantics {
                role = Role.Switch
                stateDescription = if (checked) switchOnState else switchOffState
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun LanguagePickerDialog(
    currentCode: String,
    languages: List<LanguageOption>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val titleText = stringResource(R.string.settings_language)
    val cancelText = stringResource(R.string.cancel)
    val selectedDesc = stringResource(R.string.item_selected)
    val selectedState = stringResource(R.string.switch_state_on)
    val notSelectedState = stringResource(R.string.switch_state_off)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                titleText,
                modifier = Modifier.semantics { heading() }
            )
        },
        text = {
            Column {
                languages.forEach { option ->
                    val isSelected = option.code == currentCode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option.code) }
                            .padding(vertical = 12.dp)
                            .semantics {
                                role = Role.RadioButton
                                stateDescription = if (isSelected) selectedState else notSelectedState
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            option.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = selectedDesc,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.semantics { role = Role.Button }
            ) {
                Text(cancelText)
            }
        }
    )
}
