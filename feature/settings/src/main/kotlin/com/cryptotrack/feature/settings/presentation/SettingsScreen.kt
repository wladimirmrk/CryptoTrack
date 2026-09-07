package com.cryptotrack.feature.settings.presentation

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cryptotrack.core.designsystem.component.SectionHeader
import com.cryptotrack.feature.settings.R

/**
 * Settings (макет fddb4179…): секции Preferences (Currency, Dark Mode, Refresh Interval)
 * и System (API Connection, About, Version). Бренд — CryptoTrack (ADR-008).
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val versionText = rememberAppVersionText()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        SettingsHeader(onBack = onBack)

        SectionHeader(title = stringResource(R.string.preferences_section))
        SettingsRow(
            icon = Icons.Filled.Payments,
            title = stringResource(R.string.currency_title),
            subtitle = stringResource(R.string.currency_subtitle),
            valueText = stringResource(
                R.string.currency_value,
                state.currency.name,
                state.currency.symbol,
            ),
            onClick = null, // Currency selector — следующий этап фазы.
            showChevron = true,
        )
        SettingsRow(
            icon = Icons.Filled.DarkMode,
            title = stringResource(R.string.dark_mode_title),
            subtitle = stringResource(R.string.dark_mode_subtitle),
            onClick = null,
            showChevron = false,
        ) {
            Switch(checked = state.isDarkMode, onCheckedChange = null)
        }
        SettingsRow(
            icon = Icons.Filled.Update,
            title = stringResource(R.string.refresh_interval_title),
            subtitle = stringResource(R.string.refresh_interval_subtitle),
            valueText = state.refreshInterval.label,
            onClick = null, // Refresh interval selector — следующий этап фазы.
            showChevron = true,
        )

        SectionHeader(title = stringResource(R.string.system_section))
        SettingsRow(
            icon = Icons.Filled.Api,
            title = stringResource(R.string.api_connection_title),
            subtitle = null,
            valueText = stringResource(R.string.api_connection_checking),
            // Макет показывает chevron, но не задаёт цель перехода (допущение в design-analysis).
            onClick = null,
            showChevron = true,
        )
        SettingsRow(
            icon = Icons.Filled.Info,
            title = stringResource(R.string.about_title),
            subtitle = stringResource(R.string.about_subtitle),
            onClick = null, // About-экран — следующий этап фазы.
            showChevron = true,
        )
        SettingsRow(
            icon = null,
            title = stringResource(R.string.version_title),
            subtitle = null,
            valueText = versionText,
            onClick = null,
            showChevron = false,
        )
    }
}

@Composable
private fun SettingsHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back_cd),
            )
        }
        Text(text = stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge)
    }
}

/**
 * Строка настроек по макету: иконка, заголовок + подпись, значение справа,
 * chevron или switch. Строка Version — без иконки и chevron, некликабельна.
 */
@Composable
private fun SettingsRow(
    icon: ImageVector?,
    title: String,
    subtitle: String?,
    valueText: String? = null,
    onClick: (() -> Unit)?,
    showChevron: Boolean,
    trailing: (@Composable () -> Unit)? = null,
) {
    val clickableModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        modifier = clickableModifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (valueText != null) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        trailing?.invoke()
    }
}
