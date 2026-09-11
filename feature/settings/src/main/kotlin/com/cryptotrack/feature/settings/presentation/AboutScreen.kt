package com.cryptotrack.feature.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptotrack.core.navigation.AboutTopic
import com.cryptotrack.feature.settings.R

/**
 * About (цель строки About макета Settings, допущение A-4): список разделов
 * Terms / Privacy / Licenses (каждый — статичный placeholder-экран) и версия внизу.
 */
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onTopicClick: (AboutTopic) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        AboutHeader(title = stringResource(R.string.about_title), onBack = onBack)
        AboutTopic.entries.forEach { topic ->
            AboutRow(
                title = stringResource(topic.titleRes()),
                onClick = { onTopicClick(topic) },
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.version_title) + " · " + rememberAppVersionText(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        )
    }
}

/**
 * Статичный текстовый экран раздела About (короткий placeholder — контент
 * макетом не задан, допущение A-4).
 */
@Composable
fun AboutTopicScreen(
    topic: AboutTopic,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        AboutHeader(title = stringResource(topic.titleRes()), onBack = onBack)
        Text(
            text = stringResource(topic.bodyRes()),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun AboutHeader(title: String, onBack: () -> Unit) {
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
        Text(text = title, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun AboutRow(
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun AboutTopic.titleRes(): Int = when (this) {
    AboutTopic.Terms -> R.string.topic_terms_title
    AboutTopic.Privacy -> R.string.topic_privacy_title
    AboutTopic.Licenses -> R.string.topic_licenses_title
}

private fun AboutTopic.bodyRes(): Int = when (this) {
    AboutTopic.Terms -> R.string.topic_terms_body
    AboutTopic.Privacy -> R.string.topic_privacy_body
    AboutTopic.Licenses -> R.string.topic_licenses_body
}
