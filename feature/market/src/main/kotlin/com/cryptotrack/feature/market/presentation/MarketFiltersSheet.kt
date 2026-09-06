package com.cryptotrack.feature.market.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cryptotrack.core.designsystem.component.ApexButton
import com.cryptotrack.core.designsystem.component.ApexButtonStyle
import com.cryptotrack.feature.market.domain.MarketFilter
import com.cryptotrack.feature.market.domain.MarketSortBy
import com.cryptotrack.feature.market.domain.SortOrder

/**
 * Шит фильтров (макет Market Filters): Sort By (radio), Order, Reset, Apply Filters.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketFiltersSheet(
    currentFilter: MarketFilter,
    onApply: (MarketFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    var sortBy by remember { mutableStateOf(currentFilter.sortBy) }
    var order by remember { mutableStateOf(currentFilter.order) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "Filters", style = MaterialTheme.typography.titleLarge)
            }

            Text(
                text = "Sort By",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp),
            )
            MarketSortBy.entries.forEach { option ->
                OptionRow(
                    label = option.label,
                    selected = sortBy == option,
                    onClick = { sortBy = option },
                )
            }

            Text(
                text = "Order",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp),
            )
            OptionRow(
                label = SortOrder.DESCENDING.label,
                selected = order == SortOrder.DESCENDING,
                onClick = { order = SortOrder.DESCENDING },
            )
            OptionRow(
                label = SortOrder.ASCENDING.label,
                selected = order == SortOrder.ASCENDING,
                onClick = { order = SortOrder.ASCENDING },
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ApexButton(
                    text = "Reset",
                    onClick = {
                        sortBy = MarketSortBy.MARKET_CAP
                        order = SortOrder.DESCENDING
                    },
                    style = ApexButtonStyle.Ghost,
                )
                ApexButton(
                    text = "Apply Filters",
                    onClick = { onApply(MarketFilter(sortBy = sortBy, order = order)) },
                    style = ApexButtonStyle.Primary,
                )
            }
        }
    }
}

@Composable
private fun OptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Icon(
            imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            contentDescription = null,
            tint = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                Color(0xFFC3C5D9)
            },
        )
    }
}
