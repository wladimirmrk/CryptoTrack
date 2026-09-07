package com.cryptotrack.feature.assetentry

import com.cryptotrack.core.data.repository.MarketRepository
import com.cryptotrack.core.data.repository.PortfolioRepository
import com.cryptotrack.core.data.repository.SettingsRepository
import com.cryptotrack.core.model.AppSettings
import com.cryptotrack.core.model.ChartRange
import com.cryptotrack.core.model.Coin
import com.cryptotrack.core.model.Currency
import com.cryptotrack.core.model.Holding
import com.cryptotrack.core.model.RefreshInterval
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fakes for the Asset Entry ViewModel tests, mirroring the PortfolioViewModelTest setup:
 * real Room/DAO behavior is simulated in memory (merge semantics per ADR-017).
 */
class FakePortfolioRepository : PortfolioRepository {
    val holdings = MutableStateFlow<List<Holding>>(emptyList())

    val mergedSaves = mutableListOf<Pair<String, BigDecimal>>()
    val exactSets = mutableListOf<Pair<String, BigDecimal>>()
    val removals = mutableListOf<String>()

    override fun observeHoldings(): Flow<List<Holding>> = holdings

    override fun observeHolding(coinId: String): Flow<Holding?> =
        holdings.map { list -> list.find { it.coinId == coinId } }

    override suspend fun saveHolding(
        coinId: String,
        amount: BigDecimal,
    ) {
        mergedSaves += coinId to amount
        val current = holdings.value.find { it.coinId == coinId }
        holdings.value = if (current == null) {
            holdings.value + Holding(coinId, amount, updatedAtMillis = 0L)
        } else {
            holdings.value.map {
                if (it.coinId == coinId) it.copy(amount = it.amount.add(amount)) else it
            }
        }
    }

    override suspend fun setHoldingAmount(
        coinId: String,
        amount: BigDecimal,
    ) {
        exactSets += coinId to amount
        val exists = holdings.value.any { it.coinId == coinId }
        holdings.value = if (exists) {
            holdings.value.map {
                if (it.coinId == coinId) it.copy(amount = amount) else it
            }
        } else {
            holdings.value + Holding(coinId, amount, updatedAtMillis = 0L)
        }
    }

    override suspend fun removeHolding(coinId: String) {
        removals += coinId
        holdings.value = holdings.value.filterNot { it.coinId == coinId }
    }
}

class FakeMarketRepository : MarketRepository {
    val coins = MutableStateFlow<List<Coin>>(emptyList())

    override fun observeMarkets(currency: String): Flow<List<Coin>> = coins

    override suspend fun refreshMarkets(
        currency: String,
        page: Int,
        perPage: Int,
    ) = throw NotImplementedError()

    override fun observeMarketLastUpdated(currency: String) = throw NotImplementedError()

    override fun observeCoin(
        coinId: String,
        currency: String,
    ): Flow<Coin?> = coins.map { list -> list.find { it.id == coinId } }

    override suspend fun getCoin(
        coinId: String,
        currency: String,
    ): Coin? = coins.value.find { it.id == coinId }

    override fun observeCoinDetails(
        coinId: String,
        currency: String,
    ) = throw NotImplementedError()

    override suspend fun refreshCoinDetails(
        coinId: String,
        currency: String,
    ) = throw NotImplementedError()

    override fun observePriceHistory(
        coinId: String,
        currency: String,
        range: ChartRange,
    ) = throw NotImplementedError()

    override suspend fun refreshPriceHistory(
        coinId: String,
        currency: String,
        range: ChartRange,
    ) = throw NotImplementedError()

    override suspend fun searchCoins(
        query: String,
        currency: String,
    ) = throw NotImplementedError()

    override suspend fun pingApi() = throw NotImplementedError()
}

class FakeSettingsRepository : SettingsRepository {
    override val settings = MutableStateFlow(AppSettings())

    override suspend fun setCurrency(currency: Currency) {
        settings.value = settings.value.copy(currency = currency)
    }

    override suspend fun setDarkMode(enabled: Boolean) = Unit

    override suspend fun setRefreshInterval(interval: RefreshInterval) = Unit
}

fun coin(
    id: String,
    price: String,
    change: Double,
) = Coin(
    id = id,
    symbol = id.uppercase(),
    name = id,
    imageUrl = null,
    currentPrice = BigDecimal(price),
    priceChangePercentage24h = change,
    marketCap = null,
    marketCapRank = null,
    totalVolume = null,
    high24h = null,
    low24h = null,
    circulatingSupply = null,
)

fun holding(
    coinId: String,
    amount: String,
) = Holding(coinId = coinId, amount = BigDecimal(amount), updatedAtMillis = 0L)
