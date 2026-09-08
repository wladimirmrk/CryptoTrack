package com.cryptotrack.feature.search.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cryptotrack.core.designsystem.component.ApexSearchBar
import com.cryptotrack.core.designsystem.component.CoinListItem
import com.cryptotrack.feature.search.R

/**
 * Search Results (макет): back + бренд + settings-иконка (единственный вход в
 * Settings, ADR-009), строка поиска, результаты со звёздами.
 */
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onCoinClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Row(
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
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.settings_cd),
                )
            }
        }

        ApexSearchBar(
            query = state.query,
            onQueryChange = viewModel::onQueryChange,
            hint = stringResource(R.string.search_hint),
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.isEmptyResults) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.search_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Column
        }

        LazyColumn {
            items(state.results, key = { it.id }) { coin ->
                val isFavorite = state.isFavorite(coin.id)
                CoinListItem(
                    name = coin.name,
                    symbol = coin.symbol,
                    priceText = com.cryptotrack.core.common.format.MoneyFormat.format(
                        coin.currentPrice,
                        state.currencySymbol,
                    ),
                    changeText = com.cryptotrack.core.common.format.MoneyFormat.percent(coin.priceChangePercentage24h),
                    isUp = coin.priceChangePercentage24h >= 0,
                    imageUrl = coin.imageUrl,
                    favoriteIcon = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                    onFavoriteClick = { viewModel.onFavoriteToggle(coin.id, !isFavorite) },
                    onClick = { onCoinClick(coin.id) },
                )
            }
        }
    }
}
