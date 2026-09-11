package com.cryptotrack.app

import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Top-level navigation: the real NavHost starts on Splash (auto-navigates to
 * Market, ADR-010), then the bottom bar switches Market / Portfolio / Favorites.
 * Runs inside the real Hilt MainActivity so feature graphs resolve normally.
 * Tab switching is asserted via bottom bar selection state (network-independent);
 * text checks use chrome strings that render in every screen state.
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TopLevelNavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        // The test clock drives LaunchedEffect timers (splash auto-navigation):
        // advance it manually so waiting for text deterministically moves time too.
        composeRule.mainClock.autoAdvance = false
    }

    private fun waitUntilText(text: String, timeoutMillis: Long = 10_000) {
        composeRule.waitUntil(timeoutMillis) {
            composeRule.mainClock.advanceTimeBy(100)
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitUntilTabSelected(label: String, timeoutMillis: Long = 10_000) {
        composeRule.waitUntil(timeoutMillis) {
            composeRule.mainClock.advanceTimeBy(100)
            composeRule.onAllNodesWithText(label)
                .filter(hasClickAction().and(isSelected()))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun tapBottomBarItem(label: String) {
        composeRule.onAllNodesWithText(label)
            .filter(hasClickAction())
            .onFirst()
            .performClick()
    }

    @Test
    fun splashNavigatesToMarketThenBottomBarSwitchesTabs() {
        waitUntilText("Market Overview")

        tapBottomBarItem("Portfolio")
        waitUntilTabSelected("Portfolio")

        tapBottomBarItem("Favorites")
        waitUntilTabSelected("Favorites")
        waitUntilText("No favorite cryptocurrencies yet")

        tapBottomBarItem("Market")
        waitUntilTabSelected("Market")
    }
}
