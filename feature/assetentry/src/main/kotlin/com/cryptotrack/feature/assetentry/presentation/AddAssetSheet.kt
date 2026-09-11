package com.cryptotrack.feature.assetentry.presentation

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cryptotrack.core.designsystem.component.ApexButton
import com.cryptotrack.core.designsystem.component.ApexButtonStyle
import com.cryptotrack.core.designsystem.component.ChangeText
import com.cryptotrack.core.designsystem.component.CoinIcon
import com.cryptotrack.core.designsystem.component.PriceText
import com.cryptotrack.core.designsystem.component.SkeletonBox
import com.cryptotrack.core.designsystem.theme.NumericTextStyle
import com.cryptotrack.feature.assetentry.R

/**
 * Add to Portfolio (mockup aa507857): modal bottom sheet — close header, "Add Asset"
 * title, coin info row (icon, name+ticker, price, colored 24h change), amount field,
 * live Estimated Total Value, Cancel / Save Asset footer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetSheet(
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddAssetViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Save completed → navigate back to Portfolio (flow F2, wired in the nav graph).
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }

    ModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        if (state.isLoading) {
            AddAssetSkeleton()
        } else {
            AddAssetForm(
                state = state,
                onAmountChange = viewModel::onAmountChange,
                onSave = viewModel::onSave,
                onCancel = onDismiss,
            )
        }
    }
}

@Composable
@VisibleForTesting
internal fun AddAssetForm(
    state: AddAssetUiState,
    onAmountChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.add_asset_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.close_cd),
                )
            }
        }

        CoinInfoRow(state = state)

        Text(
            text = stringResource(R.string.amount_owned_label, state.coinSymbol),
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
            placeholder = { Text(text = stringResource(R.string.amount_hint), style = NumericTextStyle) },
            isError = state.amountError != null,
            supportingText = {
                AmountErrorText(error = state.amountError)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )

        EstimatedValueBlock(valueText = state.estimatedValueText)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ApexButton(
                text = stringResource(R.string.cancel),
                onClick = onCancel,
                style = ApexButtonStyle.Secondary,
                modifier = Modifier.weight(1f),
            )
            ApexButton(
                text = stringResource(R.string.save_asset),
                onClick = onSave,
                style = ApexButtonStyle.Primary,
                enabled = state.isSaveEnabled,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** Coin info row: icon, name+ticker, price and colored "+2.4% 24h" on the right. */
@Composable
private fun CoinInfoRow(state: AddAssetUiState) {
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
            PriceText(text = state.priceText ?: stringResource(R.string.value_unavailable))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val change = state.changeText
                if (change != null) {
                    ChangeText(percentText = change, isUp = state.isPriceUp)
                    Text(
                        text = "24h",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                } else {
                    Text(
                        text = stringResource(R.string.value_unavailable),
                        style = NumericTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** Inline error under the amount field; shared with the Edit Asset screen. */
@Composable
internal fun AmountErrorText(error: AmountInputError?) {
    val text = when (error) {
        AmountInputError.InvalidFormat -> stringResource(R.string.amount_error_invalid)
        AmountInputError.NotPositive -> stringResource(R.string.amount_error_not_positive)
        null -> return
    }
    Text(text = text, style = MaterialTheme.typography.labelSmall)
}

/** Estimated Total Value block: live amount x current market price. */
@Composable
private fun EstimatedValueBlock(valueText: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.shapes.large)
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.estimated_total_value),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = valueText ?: stringResource(R.string.value_unavailable),
            style = NumericTextStyle.copy(fontSize = 24.sp, lineHeight = 32.sp),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/** Skeleton while the coin data has not arrived yet (design.md §1.5). */
@Composable
private fun AddAssetSkeleton() {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonBox(modifier = Modifier.weight(0.4f).height(24.dp))
            Spacer(modifier = Modifier.weight(1f))
            SkeletonBox(modifier = Modifier.height(24.dp).fillMaxWidth(0.15f))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkeletonBox(modifier = Modifier.height(40.dp).fillMaxWidth(0.12f))
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp))
                Spacer(modifier = Modifier.height(6.dp))
                SkeletonBox(modifier = Modifier.fillMaxWidth(0.25f).height(12.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(56.dp))
        Spacer(modifier = Modifier.height(16.dp))
        SkeletonBox(modifier = Modifier.fillMaxWidth().height(88.dp))
    }
}
