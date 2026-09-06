package com.cryptotrack.feature.assetentry.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cryptotrack.core.navigation.EditAssetRoute
import com.cryptotrack.feature.assetentry.presentation.EditAssetScreen

/** Edit Asset — full screen destination with the holding coinId argument. */
fun NavGraphBuilder.editAssetGraph(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onRemoved: () -> Unit,
) {
    composable<EditAssetRoute> {
        EditAssetScreen(onBack = onBack, onSaved = onSaved, onRemoved = onRemoved)
    }
}
