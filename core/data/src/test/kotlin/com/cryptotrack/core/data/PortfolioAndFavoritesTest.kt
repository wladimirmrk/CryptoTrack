package com.cryptotrack.core.data

import com.cryptotrack.core.data.impl.FavoritesRepositoryImpl
import com.cryptotrack.core.data.impl.PortfolioRepositoryImpl
import com.cryptotrack.core.data.database.entity.FavoriteEntity
import com.cryptotrack.core.data.database.entity.MarketCoinEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.math.BigDecimal

class PortfolioAndFavoritesTest {

    private val holdingDao = InMemoryHoldingDao()
    private val portfolio = PortfolioRepositoryImpl(
        database = TestDatabase(holding = holdingDao),
        timeProvider = FakeTimeProvider(now = 42L),
    )

    @Test
    fun `saveHolding merges repeated additions`() = runTest {
        portfolio.saveHolding("bitcoin", BigDecimal("0.5"))
        portfolio.saveHolding("bitcoin", BigDecimal("1.0"))

        val holding = portfolio.observeHolding("bitcoin").first()

        assertThat(holding?.amount?.toPlainString()).isEqualTo("1.5")
    }

    @Test
    fun `setHoldingAmount replaces value`() = runTest {
        portfolio.saveHolding("bitcoin", BigDecimal("0.5"))

        portfolio.setHoldingAmount("bitcoin", BigDecimal("2.0"))

        val holding = portfolio.observeHolding("bitcoin").first()
        assertThat(holding?.amount?.toPlainString()).isEqualTo("2.0")
    }

    @Test
    fun `removeHolding deletes entry`() = runTest {
        portfolio.saveHolding("bitcoin", BigDecimal("0.5"))

        portfolio.removeHolding("bitcoin")

        assertThat(portfolio.observeHoldings().first()).isEmpty()
    }
}

class FavoritesRepositoryTest {

    private val favoriteDao = InMemoryFavoriteDao()
    private val marketDao = InMemoryMarketDao()
    private val repository = FavoritesRepositoryImpl(
        database = TestDatabase(favorite = favoriteDao, market = marketDao),
        timeProvider = FakeTimeProvider(now = 42L),
    )

    @Test
    fun `observeFavorites joins only coins present in cache`() = runTest {
        favoriteDao.add(FavoriteEntity("bitcoin", addedAtMillis = 1L))
        favoriteDao.add(FavoriteEntity("not-in-cache", addedAtMillis = 2L))
        marketDao.upsertAll(
            listOf(
                MarketCoinEntity(
                    coinId = "bitcoin", currency = "usd", symbol = "BTC", name = "Bitcoin",
                    imageUrl = null, currentPrice = "64000", changePercent24h = 1.0,
                    marketCap = null, marketCapRank = null, totalVolume = null,
                    high24h = null, low24h = null, circulatingSupply = null,
                ),
            ),
        )

        val favorites = repository.observeFavorites("usd").first()

        assertThat(favorites.map { it.id }).containsExactly("bitcoin")
        assertThat(favorites.single().currentPrice.toPlainString()).isEqualTo("64000")
    }

    @Test
    fun `setFavorite toggles state`() = runTest {
        repository.setFavorite("ethereum", isFavorite = true)
        assertThat(repository.observeIsFavorite("ethereum").first()).isTrue()

        repository.setFavorite("ethereum", isFavorite = false)
        assertThat(repository.observeIsFavorite("ethereum").first()).isFalse()
    }
}
