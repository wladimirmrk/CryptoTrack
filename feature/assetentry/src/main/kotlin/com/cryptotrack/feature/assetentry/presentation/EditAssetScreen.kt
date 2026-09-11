package com.cryptotrack.feature.assetentry.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cryptotrack.core.designsystem.component.ApexButton
import com.cryptotrack.core.designsystem.component.ApexButtonStyle
import com.cryptotrack.core.designsystem.component.CoinIcon
import com.cryptotrack.core.designsystem.component.PriceText
import com.cryptotrack.core.designsystem.component.SkeletonBox
import com.cryptotrack.core.designsystem.theme.NumericTextStyle
import com.cryptotrack.feature.assetentry.R

/**
 * Edit Asset (mockup cf7756ee): full screen — back + title, coin identification with
 * current price, holdings amount field with ticker suffix, Total Value Preview with
 * "Based on current market price" note, Save Changes / Remove Asset buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAssetScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onRemoved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditAssetViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }
    LaunchedEffect(state.isRemoved) {
        if (state.isRemoved) onRemoved()
    }

    Column(modifier = modifier.fillMaxSize()) {
        EditTopBar(onBack = onBack)
        when {
            state.isLoading -> EditAssetSkeleton()

            state.isNotFound -> AssetNotFoundState(onBack = onBack)

            else -> EditAssetContent(
                state = state,
                onAmountChange = viewModel::onAmountChange,
                onSave = viewModel::onSave,
                onRemoveClick = viewModel::onDeleteClick,
            )
        }
    }

    // Delete Confirmation dialog (mockup c7b9c7e7) over the Edit Asset screen.
    if (state.showDeleteConfirmation) {
        DeleteConfirmationDialog(
            coinName = state.coinName,
            onConfirm = viewModel::onConfirmDelete,
            onDismiss = viewModel::onDismissDelete,
        )
    }
}

@Composable
private fun EditTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back_cd),
            )
        }
        Text(
            text = stringResource(R.string.edit_asset_title),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
private fun EditAssetContent(
    state: EditAssetUiState,
    onAmountChange: (String) -> Unit,
    onSave: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        CoinIdentificationRow(state = state)

        Text(
            text = stringResource(R.string.holdings_amount_label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp),
        )
        OutlinedTextField(
            value = state.amountInput,
            onValueChange = onAmountChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            isError = state.amountError != null,
            supportingText = { AmountErrorText(error = state.amountError) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            suffix = { Text(text = state.coinSymbol, style = NumericTextStyle) },
        )

        TotalValuePreviewBlock(
            valueText = state.estimatedValueText,
        )

        ApexButton(
            text = stringResource(R.string.save_changes),
            onClick = onSave,
            style = ApexButtonStyle.Primary,
            enabled = state.isSaveEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
        )
        DestructiveButton(
            text = stringResource(R.string.remove_asset),
            onClick = onRemoveClick,
            enabled = !state.isDeleting && !state.isRemoved,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        )
    }
}

/** Coin identification + "Current Price" on the right (mockup Edit Asset). */
@Composable
private fun CoinIdentificationRow(state: EditAssetUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoinIcon(coinSymbol = state.coinSymbol, imageUrl = state.coinImageUrl)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(
                text = state.coinName.ifEmpty { state.coinSymbol },
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = state.coinSymbol,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stringResource(R.string.current_price),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PriceText(
                text = state.currentPriceText ?: stringResource(R.string.value_unavailable),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

/** "Total Value Preview" block: Estimated value + info note (mockup Edit Asset). */
@Composable
private fun TotalValuePreviewBlock(valueText: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            // Matches the Add Asset block (surfaceContainerHigh) for consistency.
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.large)
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.total_value_preview),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.estimated),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = valueText ?: stringResource(R.string.value_unavailable),
                style = NumericTextStyle.copy(fontSize = 20.sp, lineHeight = 28.sp),
            )
        }
        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = stringResource(R.string.based_on_current_price),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

/** Destructive action button (Remove Asset): solid error colors, delete icon. */
@Composable
private fun DestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
        ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

/** Holding missing in the local portfolio (e.g. removed by another flow). */
@Composable
private fun AssetNotFoundState(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.asset_not_found),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        ApexButton(
            text = stringResource(R.string.go_back),
            onClick = onBack,
            style = ApexButtonStyle.Secondary,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

/** Skeleton while the holding/coin data has not arrived yet (design.md §1.5). */
@Composable
private fun EditAssetSkeleton() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonBox(modifier = Modifier.size(40.dp))
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp))
                Spacer(modifier = Modifier.height(6.dp))
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.25f).height(12.dp))
            }
            SkeletonBox(modifier = Modifier.width(80.dp).height(16.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(56.dp))
        Spacer(modifier = Modifier.height(16.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(110.dp))
        Spacer(modifier = Modifier.height(24.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(44.dp))
    }
}
