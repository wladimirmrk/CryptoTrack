package com.cryptotrack.feature.assetentry

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cryptotrack.core.designsystem.theme.CryptoTrackTheme
import com.cryptotrack.feature.assetentry.presentation.AddAssetForm
import com.cryptotrack.feature.assetentry.presentation.AddAssetUiState
import com.cryptotrack.feature.assetentry.presentation.AmountInputError
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the Add Asset sheet form: validation error blocks Save, and the
 * estimated total value block mirrors the computed value, rendered from
 * prepared UiState values without Hilt.
 */
@RunWith(AndroidJUnit4::class)
class AddAssetSheetTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun state(
        amountInput: String,
        amountError: AmountInputError? = null,
        estimatedValueText: String? = null,
    ) = AddAssetUiState(
        coinName = "Bitcoin",
        coinSymbol = "BTC",
        priceText = "$64,000.00",
        changeText = "+2.4%",
        amountInput = amountInput,
        amountError = amountError,
        estimatedValueText = estimatedValueText,
    )

    @Test
    fun validationErrorBlocksSave() {
        var saveClicked = false

        composeRule.setContent {
            CryptoTrackTheme {
                AddAssetForm(
                    state = state(amountInput = "abc", amountError = AmountInputError.InvalidFormat),
                    onAmountChange = {},
                    onSave = { saveClicked = true },
                    onCancel = {},
                )
            }
        }

        composeRule.onNodeWithText("Enter a valid amount").assertIsDisplayed()
        composeRule.onNodeWithText("Save Asset").assertIsDisplayed().assertIsNotEnabled()
        composeRule.onNodeWithText("Save Asset").performClick()
        assertTrue(!saveClicked)
    }

    @Test
    fun estimatedValueBlockShowsComputedValue() {
        composeRule.setContent {
            CryptoTrackTheme {
                AddAssetForm(
                    state = state(amountInput = "0.5", estimatedValueText = "$32,000.00"),
                    onAmountChange = {},
                    onSave = {},
                    onCancel = {},
                )
            }
        }

        composeRule.onNodeWithText("Estimated Total Value").assertIsDisplayed()
        composeRule.onNodeWithText("$32,000.00").assertIsDisplayed()
        composeRule.onNodeWithText("Amount Owned (BTC)").assertIsDisplayed()
        composeRule.onNodeWithText("Save Asset").assertIsDisplayed()
    }
}
