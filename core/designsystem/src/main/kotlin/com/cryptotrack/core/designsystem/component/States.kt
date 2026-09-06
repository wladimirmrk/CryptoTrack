package com.cryptotrack.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptotrack.core.designsystem.R

/** Пустое состояние (макеты Empty Portfolio / Empty Favorites). */
@Composable
fun EmptyState(
    title: String,
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    actionStyle: ApexButtonStyle = ApexButtonStyle.Primary,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionText != null && onAction != null) {
            ApexButton(
                text = actionText,
                onClick = onAction,
                style = actionStyle,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

/** Fullscreen-ошибка (макет Error State): wifi_off, заголовок, текст, Retry / Back Home. */
@Composable
fun ErrorState(
    onRetry: () -> Unit,
    onBackHome: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.WifiOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp),
        )
        Text(text = stringResource(R.string.error_title), style = MaterialTheme.typography.titleLarge)
        Text(
            text = stringResource(R.string.error_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        ApexButton(
            text = stringResource(R.string.error_retry),
            onClick = onRetry,
            modifier = Modifier.padding(top = 8.dp),
        )
        if (onBackHome != null) {
            ApexButton(
                text = stringResource(R.string.error_back_home),
                onClick = onBackHome,
                style = ApexButtonStyle.Secondary,
            )
        }
    }
}

/** Offline-баннер поверх кэшированного контента (макет Offline State). */
@Composable
fun OfflineBanner(
    lastUpdatedText: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.WifiOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer,
        )
        Column {
            Text(
                text = stringResource(R.string.offline_banner),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            if (lastUpdatedText != null) {
                Text(
                    text = stringResource(R.string.offline_last_updated, lastUpdatedText),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}
