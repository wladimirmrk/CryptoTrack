package com.cryptotrack.feature.market.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cryptotrack.core.navigation.MarketRoute
import com.cryptotrack.feature.market.presentation.MarketScreen

/**
 * Контракт навигации фичи (ADR-004): :app собирает граф, фича не знает о других фичах.
 */
fun NavGraphBuilder.marketGraph(
    onCoinClick: (String) -> Unit,
    onSearchClick: () -> Unit,
) {
    composable<MarketRoute> {
        MarketScreen(
            onCoinClick = onCoinClick,
            onSearchClick = onSearchClick,
        )
    }
}
