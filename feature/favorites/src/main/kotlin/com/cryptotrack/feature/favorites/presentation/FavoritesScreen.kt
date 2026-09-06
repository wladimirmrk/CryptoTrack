package com.cryptotrack.feature.favorites.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cryptotrack.core.designsystem.component.CoinListItem
import com.cryptotrack.core.designsystem.component.EmptyState
import com.cryptotrack.core.designsystem.component.SectionHeader
import com.cryptotrack.feature.favorites.R

/**
 * Favorites (макеты Favorites / Empty Favorites): заголовок «N Assets», карточки
 * с ценой/24h%, переключатель звезды; empty → «Browse Market».
 */
@Composable
fun FavoritesScreen(
    onCoinClick: (String) -> Unit,
    onBrowseMarket: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            SectionHeader(
                title = stringResource(R.string.favorites_title),
                subtitle = if (!state.isEmpty) state.countText else null,
                action = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.search_cd),
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (state.isEmpty) {
            EmptyState(
                title = stringResource(R.string.favorites_empty_title),
                message = stringResource(R.string.favorites_empty_message),
                actionText = stringResource(R.string.favorites_browse),
                onAction = onBrowseMarket,
                actionStyle = com.cryptotrack.core.designsystem.component.ApexButtonStyle.Primary,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(state.coins, key = { it.id }) { coin ->
                    CoinListItem(
                        name = coin.name,
                        symbol = coin.symbol,
                        priceText = com.cryptotrack.core.common.format.MoneyFormat.usd(coin.currentPrice),
                        changeText = com.cryptotrack.core.common.format.MoneyFormat.percent(coin.priceChangePercentage24h),
                        isUp = coin.priceChangePercentage24h >= 0,
                        imageUrl = coin.imageUrl,
                        favoriteIcon = Icons.Filled.Star,
                        onFavoriteClick = { viewModel.onFavoriteToggle(coin.id) },
                        onClick = { onCoinClick(coin.id) },
                    )
                }
            }
        }
    }
}
