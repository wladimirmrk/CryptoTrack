package com.cryptotrack.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

private val ICON_SIZE = 40.dp

/** Иконка коина 40×40: загрузка по URL из API, fallback — буква тикера в круге. */
@Composable
fun CoinIcon(
    coinSymbol: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = ICON_SIZE,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = coinSymbol,
                modifier = Modifier.size(size),
            )
        } else {
            Text(
                text = coinSymbol.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Заглушка иконки в скелетоне. */
@Composable
fun CoinIconSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(ICON_SIZE)
            .padding(0.dp)
            .clip(CircleShape)
            .skeletonBackground(),
    )
}
