package com.cryptotrack.core.data.impl

import com.cryptotrack.core.common.coroutines.DispatchersProvider
import com.cryptotrack.core.common.result.Result
import com.cryptotrack.core.common.result.map
import com.cryptotrack.core.common.time.TimeProvider
import com.cryptotrack.core.data.cache.CacheKeys
import com.cryptotrack.core.data.database.CryptoTrackDatabase
import com.cryptotrack.core.data.mapper.cacheMeta
import com.cryptotrack.core.data.mapper.toCoin
import com.cryptotrack.core.data.mapper.toCoinDetails
import com.cryptotrack.core.data.mapper.toEntity
import com.cryptotrack.core.data.mapper.toPricePoint
import com.cryptotrack.core.data.repository.MarketRepository
import com.cryptotrack.core.model.Coin
import com.cryptotrack.core.model.CoinDetails
import com.cryptotrack.core.model.ChartRange
import com.cryptotrack.core.model.PricePoint
import com.cryptotrack.core.network.source.MarketRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketRepositoryImpl @Inject constructor(
    private val remoteDataSource: MarketRemoteDataSource,
    database: CryptoTrackDatabase,
    private val dispatchers: DispatchersProvider,
    private val timeProvider: TimeProvider,
) : MarketRepository {

    private val marketDao = database.marketDao()
    private val coinDetailsDao = database.coinDetailsDao()
    private val priceHistoryDao = database.priceHistoryDao()
    private val cacheMetaDao = database.cacheMetaDao()

    override fun observeMarkets(currency: String): Flow<List<Coin>> =
        marketDao.observeAll(currency).map { list -> list.map { it.toCoin() } }

    override suspend fun refreshMarkets(
        currency: String,
        page: Int,
        perPage: Int,
    ): Result<Unit> = remoteDataSource.getMarkets(currency = currency, page = page, perPage = perPage)
        .map { coins ->
            withContext(dispatchers.io) {
                marketDao.upsertAll(coins.map { it.toEntity(currency) })
                cacheMetaDao.put(cacheMeta(CacheKeys.market(currency), timeProvider.nowMillis()))
            }
        }

    override fun observeMarketLastUpdated(currency: String): Flow<Long?> =
        cacheMetaDao.observe(CacheKeys.market(currency)).map { it?.lastUpdatedMillis }

    override fun observeCoin(coinId: String, currency: String): Flow<Coin?> =
        marketDao.observeAll(currency).map { list -> list.find { it.coinId == coinId }?.toCoin() }

    override suspend fun getCoin(coinId: String, currency: String): Coin? =
        marketDao.get(coinId = coinId, currency = currency)?.toCoin()

    override fun observeCoinDetails(coinId: String, currency: String): Flow<CoinDetails?> =
        coinDetailsDao.observe(coinId = coinId, currency = currency).map { it?.toCoinDetails() }

    override suspend fun refreshCoinDetails(coinId: String, currency: String): Result<Unit> =
        remoteDataSource.getCoinDetails(coinId = coinId, currency = currency).map { details ->
            withContext(dispatchers.io) {
                coinDetailsDao.upsert(details.toEntity(currency, timeProvider.nowMillis()))
                cacheMetaDao.put(cacheMeta(CacheKeys.details(coinId, currency), timeProvider.nowMillis()))
            }
        }

    override fun observePriceHistory(
        coinId: String,
        currency: String,
        range: ChartRange,
    ): Flow<List<PricePoint>> =
        priceHistoryDao.observe(
            coinId = coinId,
            currency = currency,
            range = range.name,
        ).map { points -> points.map { it.toPricePoint() } }

    override suspend fun refreshPriceHistory(
        coinId: String,
        currency: String,
        range: ChartRange,
    ): Result<Unit> = remoteDataSource.getMarketChart(
        coinId = coinId,
        currency = currency,
        range = range,
    ).map { points ->
        withContext(dispatchers.io) {
            priceHistoryDao.replaceAll(
                coinId = coinId,
                currency = currency,
                range = range.name,
                points = points.map { it.toEntity(coinId, currency, range.name) },
            )
            cacheMetaDao.put(cacheMeta(CacheKeys.history(coinId, currency, range), timeProvider.nowMillis()))
        }
    }

    override suspend fun searchCoins(query: String, currency: String): Result<List<Coin>> =
        remoteDataSource.search(query = query).map { coins ->
            coins.map { coin ->
                // Обогащаем результат ценой из кэша: в ответе /search цен нет (макеты Search Results).
                val cached = marketDao.get(coinId = coin.id, currency = currency)
                if (cached != null) {
                    cached.toCoin().copy(priceChangePercentage24h = cached.changePercent24h)
                } else {
                    coin
                }
            }
        }

    override suspend fun pingApi(): Result<Long> = remoteDataSource.ping()
}
