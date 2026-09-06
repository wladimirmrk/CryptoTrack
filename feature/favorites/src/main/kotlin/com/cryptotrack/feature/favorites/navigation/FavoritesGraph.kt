package com.cryptotrack.feature.favorites.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cryptotrack.core.navigation.FavoritesRoute
import com.cryptotrack.feature.favorites.presentation.FavoritesScreen

fun NavGraphBuilder.favoritesGraph(
    onCoinClick: (String) -> Unit,
    onBrowseMarket: () -> Unit,
    onSearchClick: () -> Unit,
) {
    composable<FavoritesRoute> {
        FavoritesScreen(
            onCoinClick = onCoinClick,
            onBrowseMarket = onBrowseMarket,
            onSearchClick = onSearchClick,
        )
    }
}
