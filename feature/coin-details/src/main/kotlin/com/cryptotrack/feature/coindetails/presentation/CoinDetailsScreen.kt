package com.cryptotrack.feature.coindetails.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cryptotrack.core.designsystem.component.ApexCard
import com.cryptotrack.core.designsystem.component.ChangeText
import com.cryptotrack.core.designsystem.component.CoinIcon
import com.cryptotrack.core.designsystem.component.ErrorState
import com.cryptotrack.core.designsystem.component.OfflineBanner
import com.cryptotrack.core.designsystem.component.PriceText
import com.cryptotrack.core.designsystem.component.SkeletonBox
import com.cryptotrack.core.designsystem.component.StatTile
import com.cryptotrack.core.model.ChartRange
import com.cryptotrack.feature.coindetails.R
import com.cryptotrack.feature.coindetails.presentation.chart.PriceChart

/**
 * Coin Details (макет): back+name+star, цена, таймфреймы+график, Market Stats,
 * About + Read More, Add to Portfolio.
 */
@Composable
fun CoinDetailsScreen(
    onBack: () -> Unit,
    onAddToPortfolio: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CoinDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        DetailsTopBar(
            name = state.name.ifEmpty { stringResource(R.string.details_title_placeholder) },
            isFavorite = state.isFavorite,
            onBack = onBack,
            onFavoriteToggle = viewModel::onFavoriteToggle,
        )

        if (state.isOffline) {
            OfflineBanner(lastUpdatedText = state.lastUpdatedText)
        }

        when {
            state.isLoading -> DetailsSkeleton()
            state.showError -> ErrorState(onRetry = viewModel::onRefresh)
            else -> DetailsContent(state = state, viewModel = viewModel, onAddToPortfolio = onAddToPortfolio)
        }
    }
}

@Composable
private fun DetailsTopBar(
    name: String,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onFavoriteToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back_cd),
            )
        }
        Text(text = name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        IconButton(onClick = onFavoriteToggle) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = stringResource(R.string.favorite_cd),
                tint = if (isFavorite) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun DetailsContent(
    state: CoinDetailsUiState,
    viewModel: CoinDetailsViewModel,
    onAddToPortfolio: (String) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CoinIcon(coinSymbol = state.symbol, imageUrl = state.imageUrl)
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(text = state.name, style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = state.symbol,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                PriceText(text = state.priceText, modifier = Modifier.padding(top = 16.dp))
                ChangeText(percentText = state.changeText, isUp = state.isUp)
            }
        }

        item {
            TimeframeTabs(
                selected = state.selectedRange,
                onSelect = viewModel::onRangeSelect,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                if (state.isChartLoading && state.chartPoints.isEmpty()) {
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = MaterialTheme.shapes.large,
                    )
                } else if (state.chartPoints.isNotEmpty()) {
                    PriceChart(
                        values = state.chartPoints,
                        isUp = state.isChartUp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                    )
                }
            }
        }

        item {
            ApexCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.market_stats),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Row(modifier = Modifier.padding(top = 8.dp)) {
                        StatTile(
                            label = stringResource(R.string.market_cap),
                            value = listOfNotNull(state.marketCapText, state.marketCapRank?.let { "Rank #$it" })
                                .joinToString(" · "),
                            modifier = Modifier.weight(1f),
                        )
                        StatTile(
                            label = stringResource(R.string.volume_24h),
                            value = state.volumeText ?: "—",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row {
                        StatTile(
                            label = stringResource(R.string.circ_supply),
                            value = state.supplyText ?: "—",
                            modifier = Modifier.weight(1f),
                        )
                        StatTile(
                            label = stringResource(R.string.high_24h),
                            value = state.highText ?: "—",
                            modifier = Modifier.weight(1f),
                        )
                        StatTile(
                            label = stringResource(R.string.low_24h),
                            value = state.lowText ?: "—",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        if (state.about != null) {
            item { AboutSection(about = state.about) }
        }

        item {
            Button(
                onClick = { onAddToPortfolio(state.coinId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(imageVector = Icons.Filled.AddCircle, contentDescription = null)
                Text(
                    text = stringResource(R.string.add_to_portfolio),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun TimeframeTabs(
    selected: ChartRange,
    onSelect: (ChartRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        ChartRange.entries.forEach { range ->
            val isSelected = range == selected
            Text(
                text = range.label,
                style = if (isSelected) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier
                    .clickable { onSelect(range) }
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        },
                        MaterialTheme.shapes.small,
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun AboutSection(about: String) {
    var expanded by remember { mutableStateOf(false) }
    ApexCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.about_title), style = MaterialTheme.typography.titleMedium)
            Text(
                text = about,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else 4,
                modifier = Modifier.padding(top = 8.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { expanded = !expanded },
            ) {
                Text(
                    text = stringResource(if (expanded) R.string.read_less else R.string.read_more),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun DetailsSkeleton() {
    Column(modifier = Modifier.padding(16.dp)) {
        SkeletonBox(modifier = Modifier.fillMaxWidth(0.5f).height(24.dp))
        Spacer(modifier = Modifier.height(12.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth(0.35f).height(20.dp))
        Spacer(modifier = Modifier.height(24.dp))
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = MaterialTheme.shapes.large,
        )
        Spacer(modifier = Modifier.height(24.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(140.dp), shape = MaterialTheme.shapes.large)
    }
}
