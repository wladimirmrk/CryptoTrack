package com.cryptotrack.feature.portfolio.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cryptotrack.core.navigation.PortfolioRoute
import com.cryptotrack.feature.portfolio.presentation.PortfolioScreen

fun NavGraphBuilder.portfolioGraph(
    onCoinClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onAddFirstAsset: () -> Unit,
    onHowToFund: () -> Unit,
) {
    composable<PortfolioRoute> {
        PortfolioScreen(
            onCoinClick = onCoinClick,
            onSearchClick = onSearchClick,
            onAddFirstAsset = onAddFirstAsset,
            onHowToFund = onHowToFund,
        )
    }
}
