package com.cryptotrack.feature.market.presentation

import androidx.annotation.VisibleForTesting
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.cryptotrack.feature.market.R
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cryptotrack.core.designsystem.component.CoinListItem
import com.cryptotrack.core.designsystem.component.ErrorState
import com.cryptotrack.core.designsystem.component.OfflineBanner
import com.cryptotrack.core.designsystem.component.SectionHeader
import com.cryptotrack.core.designsystem.component.SkeletonBox
import com.cryptotrack.feature.market.domain.MarketSortBy

/** Market Home (макет Market Home): бренд+поиск, Market Overview, фильтры/сортировка, список коинов. */
@Composable
fun MarketScreen(
    onCoinClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MarketViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        MarketTopBar(onSearchClick = onSearchClick)

        if (state.isOffline) {
            OfflineBanner(lastUpdatedText = state.lastUpdatedText)
        }

        SectionHeader(
            title = stringResource(R.string.market_title),
            subtitle = stringResource(R.string.market_subtitle),
        )

        MarketToolbar(
            filter = state.filter,
            showSortMenu = showSortMenu,
            onSortMenuChange = { showSortMenu = it },
            onFiltersClick = { showFilters = true },
        )

        MarketStateContent(
            state = state,
            onCoinClick = onCoinClick,
            onFavoriteToggle = viewModel::onFavoriteToggle,
            onRetry = { viewModel.onRefresh(force = true) },
        )
    }

    if (showFilters) {
        MarketFiltersSheet(
            currentFilter = state.filter,
            onApply = {
                viewModel.onApplyFilter(it)
                showFilters = false
            },
            onDismiss = { showFilters = false },
        )
    }
}

@Composable
private fun MarketTopBar(onSearchClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = onSearchClick) {
            Icon(imageVector = Icons.Filled.Search, contentDescription = stringResource(R.string.search_cd))
        }
    }
}

@Composable
private fun MarketToolbar(
    filter: com.cryptotrack.feature.market.domain.MarketFilter,
    showSortMenu: Boolean,
    onSortMenuChange: (Boolean) -> Unit,
    onFiltersClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onFiltersClick) {
            Icon(imageVector = Icons.Filled.FilterList, contentDescription = stringResource(R.string.filters_cd))
        }
        Box {
            IconButton(onClick = { onSortMenuChange(true) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = stringResource(R.string.sort_cd),
                )
            }
            DropdownMenu(expanded = showSortMenu, onDismissRequest = { onSortMenuChange(false) }) {
                MarketSortBy.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option.label) },
                        trailingIcon = {
                            if (option == filter.sortBy) {
                                Icon(imageVector = Icons.Filled.Check, contentDescription = null)
                            }
                        },
                        onClick = { onSortMenuChange(false) },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.sort_by_label, filter.sortBy.label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
        }
    }
}

/** State branches of the Market Home body (extracted for VM-free UI tests). */
@Composable
@VisibleForTesting
internal fun MarketStateContent(
    state: MarketUiState,
    onCoinClick: (String) -> Unit,
    onFavoriteToggle: (String, Boolean) -> Unit,
    onRetry: () -> Unit,
) {
    when {
        state.isLoading -> MarketSkeleton()
        state.showError -> ErrorState(onRetry = onRetry)
        else -> MarketList(state = state, onCoinClick = onCoinClick, onFavoriteToggle = onFavoriteToggle)
    }
}

@Composable
@VisibleForTesting
internal fun MarketList(
    state: MarketUiState,
    onCoinClick: (String) -> Unit,
    onFavoriteToggle: (String, Boolean) -> Unit,
) {
    if (state.coins.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.market_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(state.coins, key = { it.coinId }) { coin ->
            CoinListItem(
                name = coin.name,
                symbol = coin.symbol,
                priceText = coin.priceText,
                changeText = coin.changeText,
                isUp = coin.isUp,
                imageUrl = coin.imageUrl,
                favoriteIcon = if (coin.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                onFavoriteClick = { onFavoriteToggle(coin.coinId, !coin.isFavorite) },
                onClick = { onCoinClick(coin.coinId) },
            )
        }
    }
}

/** Skeleton-список (макет Market Loading State): каркас экрана + пульсирующие строки. */
@Composable
private fun MarketSkeleton() {
    Column(modifier = Modifier.fillMaxSize()) {
        repeat(SKELETON_ROWS) {
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

private const val SKELETON_ROWS = 8
