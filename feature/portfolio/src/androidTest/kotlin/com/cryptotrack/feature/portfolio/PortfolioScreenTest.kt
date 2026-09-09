package com.cryptotrack.feature.portfolio

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cryptotrack.core.designsystem.theme.CryptoTrackTheme
import com.cryptotrack.feature.portfolio.presentation.PortfolioContent
import com.cryptotrack.feature.portfolio.presentation.PortfolioEmptyState
import com.cryptotrack.feature.portfolio.presentation.PortfolioSkeleton
import com.cryptotrack.feature.portfolio.presentation.PortfolioUiState
import com.cryptotrack.feature.portfolio.presentation.PositionUi
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the Portfolio Overview screen state branches (skeleton / empty /
 * content), rendered from prepared UiState values without Hilt.
 */
@RunWith(AndroidJUnit4::class)
class PortfolioScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun skeletonShowsSectionHeaders() {
        composeRule.setContent {
            CryptoTrackTheme {
                PortfolioSkeleton()
            }
        }

        composeRule.onNodeWithText("Allocation").assertIsDisplayed()
        composeRule.onNodeWithText("Your Assets").assertIsDisplayed()
    }

    @Test
    fun emptyStateShowsAddFirstAssetAndTriggersCallback() {
        var addFirstAssetClicked = false

        composeRule.setContent {
            CryptoTrackTheme {
                PortfolioEmptyState(
                    onAddFirstAsset = { addFirstAssetClicked = true },
                    onHowToFund = {},
                )
            }
        }

        composeRule.onNodeWithText("Your portfolio is empty.").assertIsDisplayed()
        composeRule.onNodeWithText("Add First Asset").assertIsDisplayed().performClick()
        assertTrue(addFirstAssetClicked)
    }

    @Test
    fun contentShowsBalanceAndPositionsAndPropagatesAssetClick() {
        var clickedCoinId: String? = null
        val state = PortfolioUiState(
            isLoading = false,
            totalBalanceText = "$12,345.67",
            change24hText = "+2.4%",
            isUp = true,
            positions =
            listOf(
                PositionUi(
                    coinId = "bitcoin",
                    name = "Bitcoin",
                    symbol = "BTC",
                    imageUrl = null,
                    amountText = "0.25 BTC",
                    valueText = "$10,000.00",
                    changeText = "+2.4%",
                    isUp = true,
                ),
            ),
        )

        composeRule.setContent {
            CryptoTrackTheme {
                PortfolioContent(state = state, onAssetClick = { clickedCoinId = it })
            }
        }

        composeRule.onNodeWithText("Total Balance").assertIsDisplayed()
        composeRule.onNodeWithText("$12,345.67").assertIsDisplayed()
        composeRule.onNodeWithText("Your Assets").assertIsDisplayed()
        composeRule.onNodeWithText("Bitcoin").assertIsDisplayed().performClick()
        assertTrue(clickedCoinId == "bitcoin")
    }
}
