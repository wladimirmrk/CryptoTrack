package com.cryptotrack.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cryptotrack.core.designsystem.theme.NumericTextStyle
import com.cryptotrack.core.designsystem.theme.priceColors

/** Цена в моноширинном начертании (JetBrains Mono, tabular figures). */
@Composable
fun PriceText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(text = text, modifier = modifier, style = NumericTextStyle)
}

/** Изменение цены за 24h: знак, цвет и стрелка — как в макетах (+2.4% с arrow_upward). */
@Composable
fun ChangeText(
    percentText: String,
    isUp: Boolean,
    modifier: Modifier = Modifier,
    arrow: ImageVector? = null,
) {
    val color = if (isUp) MaterialTheme.colorScheme.priceColors.up else MaterialTheme.colorScheme.priceColors.down
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (arrow != null) {
            Icon(
                imageVector = arrow,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            text = percentText,
            style = NumericTextStyle,
            color = color,
            modifier = Modifier.padding(start = if (arrow != null) 2.dp else 0.dp),
        )
    }
}

/** Бейдж 24h PNL на карточке Total Balance (success/error container). */
@Composable
fun ChangeBadge(
    text: String,
    isUp: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme.priceColors
    val container = if (isUp) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    Row(
        modifier = modifier
            .background(container, MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = if (isUp) colors.up else colors.down)
    }
}
