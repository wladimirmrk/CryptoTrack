package com.cryptotrack.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Строка коина (Coin List Item, design-analysis §5): иконка 40×40, имя+тикер,
 * справа цена и 24h%; опционально количество холдинга и звезда-избранное.
 */
@Composable
fun CoinListItem(
    name: String,
    symbol: String,
    priceText: String,
    changeText: String,
    isUp: Boolean,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    amountText: String? = null,
    favoriteIcon: ImageVector? = null,
    onFavoriteClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickableIfNotNull(onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoinIcon(coinSymbol = symbol, imageUrl = imageUrl)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(text = name, style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = symbol,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (amountText != null) {
                    Text(
                        text = " · $amountText",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            PriceText(text = priceText)
            ChangeText(percentText = changeText, isUp = isUp)
        }
        if (favoriteIcon != null && onFavoriteClick != null) {
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = favoriteIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primaryContainer,
                )
            }
        }
    }
}

private fun Modifier.clickableIfNotNull(onClick: (() -> Unit)?): Modifier =
    if (onClick != null) this.clickable(onClick = onClick) else this
