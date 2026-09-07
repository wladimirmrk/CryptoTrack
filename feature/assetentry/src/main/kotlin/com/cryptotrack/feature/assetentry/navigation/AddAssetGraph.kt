package com.cryptotrack.feature.assetentry.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cryptotrack.core.navigation.AddAssetRoute
import com.cryptotrack.feature.assetentry.presentation.AddAssetSheet

/** Add to Portfolio — modal bottom sheet destination with the coinId argument. */
fun NavGraphBuilder.addAssetGraph(
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
) {
    composable<AddAssetRoute> {
        AddAssetSheet(onDismiss = onDismiss, onSaved = onSaved)
    }
}
