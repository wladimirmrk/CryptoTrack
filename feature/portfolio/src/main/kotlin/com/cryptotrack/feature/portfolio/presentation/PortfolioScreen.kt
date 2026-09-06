package com.cryptotrack.feature.portfolio.presentation

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cryptotrack.core.designsystem.component.ApexButton
import com.cryptotrack.core.designsystem.component.ApexCard
import com.cryptotrack.core.designsystem.component.ChangeBadge
import com.cryptotrack.core.designsystem.component.CoinListItem
import com.cryptotrack.core.designsystem.component.ErrorState
import com.cryptotrack.core.designsystem.component.OfflineBanner
import com.cryptotrack.core.designsystem.component.SectionHeader
import com.cryptotrack.core.designsystem.component.SkeletonBox
import com.cryptotrack.core.designsystem.theme.priceColors
import com.cryptotrack.feature.portfolio.R
import java.util.Locale

/**
 * Portfolio Overview (макет): Total Balance card, Allocation (донат),
 * Your Assets, Empty Portfolio, Loading skeleton.
 */
@Composable
fun PortfolioScreen(
    onAssetClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onAddFirstAsset: () -> Unit,
    onHowToFund: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PortfolioViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        PortfolioTopBar(onSearchClick = onSearchClick)

        if (state.isOffline) {
            OfflineBanner(lastUpdatedText = state.lastUpdatedText)
        }

        when {
            state.isLoading -> PortfolioSkeleton()

            state.showError -> ErrorState(onRetry = viewModel::onRefresh)

            state.isEmpty -> PortfolioEmptyState(
                onAddFirstAsset = onAddFirstAsset,
                onHowToFund = onHowToFund,
            )

            else -> PortfolioContent(state = state, onAssetClick = onAssetClick)
        }
    }
}

@Composable
private fun PortfolioTopBar(onSearchClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = stringResource(R.string.portfolio_title), style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = onSearchClick) {
            Icon(imageVector = Icons.Filled.Search, contentDescription = stringResource(R.string.search_cd))
        }
    }
}

@Composable
private fun PortfolioContent(
    state: PortfolioUiState,
    onAssetClick: (String) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            ApexCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.total_balance),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = state.totalBalanceText,
                        style = MaterialTheme.typography.displayLarge,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropUp,
                            contentDescription = null,
                            tint = if (state.isUp) {
                                MaterialTheme.colorScheme.priceColors.up
                            } else {
                                MaterialTheme.colorScheme.priceColors.down
                            },
                        )
                        Text(
                            text = state.change24hText,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        ChangeBadge(text = stringResource(R.string.change_24h), isUp = state.isUp)
                    }
                }
            }
        }

        item {
            SectionHeader(title = stringResource(R.string.allocation))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AllocationDonut(allocations = state.allocations)
                Column(modifier = Modifier.padding(start = 24.dp)) {
                    state.allocations.take(4).forEachIndexed { legendIndex, allocation ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .backgroundDonut(legendIndex),
                            )
                            Text(
                                text = "${allocation.symbol}  ${"%.1f%%".format(Locale.US, allocation.percent)}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        item {
            SectionHeader(title = stringResource(R.string.your_assets))
        }

        items(state.positions, key = { it.coinId }) { position ->
            CoinListItem(
                name = position.name,
                symbol = position.symbol,
                priceText = position.valueText,
                changeText = position.changeText,
                isUp = position.isUp,
                imageUrl = position.imageUrl,
                amountText = position.amountText,
                onClick = { onAssetClick(position.coinId) },
            )
        }
    }
}

/** Empty Portfolio (макет): центрированный блок, «Add First Asset», ссылка «How to fund» (A-4). */
@Composable
private fun PortfolioEmptyState(
    onAddFirstAsset: () -> Unit,
    onHowToFund: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.portfolio_empty_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.portfolio_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        ApexButton(
            text = stringResource(R.string.portfolio_add_first),
            onClick = onAddFirstAsset,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = stringResource(R.string.portfolio_how_to_fund),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 16.dp)
                .clickable(onClick = onHowToFund),
        )
    }
}

/** Skeleton экрана (макет Loading State): каркас Portfolio + пульсирующие блоки вместо данных. */
@Composable
private fun PortfolioSkeleton() {
    Column(modifier = Modifier.fillMaxSize()) {
        ApexCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.25f).height(12.dp))
                Spacer(modifier = Modifier.height(12.dp))
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.55f).height(32.dp))
                Spacer(modifier = Modifier.height(12.dp))
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp))
            }
        }
        SectionHeader(title = stringResource(R.string.allocation))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonBox(modifier = Modifier.size(120.dp), shape = CircleShape)
            Column(modifier = Modifier.weight(1f).padding(start = 24.dp)) {
                repeat(3) {
                    SkeletonBox(modifier = Modifier.fillMaxWidth(0.4f).height(12.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        SectionHeader(title = stringResource(R.string.your_assets))
        repeat(SKELETON_ASSET_ROWS) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SkeletonBox(modifier = Modifier.size(40.dp), shape = MaterialTheme.shapes.extraLarge)
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    SkeletonBox(modifier = Modifier.fillMaxWidth(0.5f).height(16.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    SkeletonBox(modifier = Modifier.fillMaxWidth(0.3f).height(12.dp))
                }
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.2f).height(16.dp))
            }
        }
    }
}

private const val SKELETON_ASSET_ROWS = 4

@Composable
private fun Modifier.backgroundDonut(index: Int): Modifier {
    val colors = com.cryptotrack.feature.portfolio.presentation.donutColors
    return this.background(colors[index % colors.size], MaterialTheme.shapes.extraSmall)
}
