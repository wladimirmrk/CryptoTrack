package com.cryptotrack.feature.market

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cryptotrack.core.common.result.AppError
import com.cryptotrack.core.designsystem.theme.CryptoTrackTheme
import com.cryptotrack.feature.market.presentation.CoinUi
import com.cryptotrack.feature.market.presentation.MarketStateContent
import com.cryptotrack.feature.market.presentation.MarketUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the Market Home screen: market rows and the fullscreen error
 * state, rendered from prepared UiState values without Hilt.
 */
@RunWith(AndroidJUnit4::class)
class MarketScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val coins =
        listOf(
            CoinUi(
                coinId = "bitcoin",
                name = "Bitcoin",
                symbol = "BTC",
                imageUrl = null,
                priceText = "$64,000.00",
                changeText = "+2.4%",
                isUp = true,
                isFavorite = false,
            ),
            CoinUi(
                coinId = "ethereum",
                name = "Ethereum",
                symbol = "ETH",
                imageUrl = null,
                priceText = "$3,100.00",
                changeText = "-1.2%",
                isUp = false,
                isFavorite = true,
            ),
        )

    @Test
    fun listShowsCoinRowsAndPropagatesCoinClick() {
        var clickedCoinId: String? = null
        val state = MarketUiState(isLoading = false, coins = coins)

        composeRule.setContent {
            CryptoTrackTheme {
                MarketStateContent(
                    state = state,
                    onCoinClick = { clickedCoinId = it },
                    onFavoriteToggle = { _, _ -> },
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed()
        composeRule.onNodeWithText("$64,000.00").assertIsDisplayed()
        composeRule.onNodeWithText("Ethereum").assertIsDisplayed()
        composeRule.onNodeWithText("Bitcoin").performClick()
        assertEquals("bitcoin", clickedCoinId)
    }

    @Test
    fun errorStateShowsFullscreenErrorAndRetry() {
        var retried = false
        val state = MarketUiState(isLoading = false, error = AppError.Network)

        composeRule.setContent {
            CryptoTrackTheme {
                MarketStateContent(
                    state = state,
                    onCoinClick = {},
                    onFavoriteToggle = { _, _ -> },
                    onRetry = { retried = true },
                )
            }
        }

        composeRule.onNodeWithText("Something went wrong").assertIsDisplayed()
        composeRule.onNodeWithText("RETRY CONNECTION").assertIsDisplayed().performClick()
        assertTrue(retried)
        composeRule.onNodeWithText("Bitcoin").assertDoesNotExist()
    }
}
