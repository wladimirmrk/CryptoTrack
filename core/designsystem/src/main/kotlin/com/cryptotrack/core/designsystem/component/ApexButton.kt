package com.cryptotrack.core.designsystem.component

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ApexButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ApexButtonStyle = ApexButtonStyle.Primary,
    enabled: Boolean = true,
) {
    when (style) {
        ApexButtonStyle.Primary -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }

        ApexButtonStyle.Secondary -> OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        ) {
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }

        ApexButtonStyle.Ghost -> TextButton(onClick = onClick, modifier = modifier, enabled = enabled) {
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}
