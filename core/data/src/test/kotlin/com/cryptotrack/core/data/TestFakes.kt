package com.cryptotrack.core.data

import com.cryptotrack.core.common.time.TimeProvider
import com.cryptotrack.core.data.database.CryptoTrackDatabase
import com.cryptotrack.core.data.database.dao.CacheMetaDao
import com.cryptotrack.core.data.database.dao.CoinDetailsDao
import com.cryptotrack.core.data.database.dao.FavoriteDao
import com.cryptotrack.core.data.database.dao.HoldingDao
import com.cryptotrack.core.data.database.dao.MarketDao
import com.cryptotrack.core.data.database.dao.PriceHistoryDao
import com.cryptotrack.core.data.database.entity.CacheMetaEntity
import com.cryptotrack.core.data.database.entity.CoinDetailsEntity
import com.cryptotrack.core.data.database.entity.FavoriteEntity
import com.cryptotrack.core.data.database.entity.HoldingEntity
import com.cryptotrack.core.data.database.entity.MarketCoinEntity
import com.cryptotrack.core.data.database.entity.PriceHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeTimeProvider(
    var now: Long = 1_000L,
) : TimeProvider {
    override fun nowMillis(): Long = now
}

internal class InMemoryHoldingDao : HoldingDao {
    val store = MutableStateFlow<List<HoldingEntity>>(emptyList())

    override fun observeAll(): Flow<List<HoldingEntity>> = store

    override fun observe(coinId: String): Flow<HoldingEntity?> = store.map { list -> list.find { it.coinId == coinId } }

    override suspend fun get(coinId: String): HoldingEntity? = store.value.find { it.coinId == coinId }

    override suspend fun upsert(holding: HoldingEntity) {
        store.value = store.value.filterNot { it.coinId == holding.coinId } + holding
    }

    override suspend fun delete(coinId: String) {
        store.value = store.value.filterNot { it.coinId == coinId }
    }
}

internal class InMemoryFavoriteDao : FavoriteDao {
    val store = MutableStateFlow<List<FavoriteEntity>>(emptyList())

    override fun observeAll(): Flow<List<FavoriteEntity>> = store

    override fun observeIds(): Flow<List<String>> = store.map { list -> list.map { it.coinId } }

    override fun observeIsFavorite(coinId: String): Flow<Boolean> =
        store.map { list -> list.any { it.coinId == coinId } }

    override suspend fun add(favorite: FavoriteEntity) {
        store.value = store.value.filterNot { it.coinId == favorite.coinId } + favorite
    }

    override suspend fun remove(coinId: String) {
        store.value = store.value.filterNot { it.coinId == coinId }
    }
}

internal class InMemoryMarketDao : MarketDao {
    val store = MutableStateFlow<List<MarketCoinEntity>>(emptyList())

    override fun observeAll(currency: String): Flow<List<MarketCoinEntity>> = store.map { list ->
        list.filter {
        it.currency == currency
    }
    }

    override suspend fun get(
        coinId: String,
        currency: String,
    ): MarketCoinEntity? = store.value.find { it.coinId == coinId && it.currency == currency }

    override suspend fun upsertAll(coins: List<MarketCoinEntity>) {
        val merged = store.value.associateBy { it.coinId to it.currency }.toMutableMap()
        coins.forEach { merged[it.coinId to it.currency] = it }
        store.value = merged.values.toList()
    }
}

internal class InMemoryCoinDetailsDao : CoinDetailsDao {
    val store = MutableStateFlow<Map<String, CoinDetailsEntity>>(emptyMap())

    override fun observe(
        coinId: String,
        currency: String,
    ): Flow<CoinDetailsEntity?> = store.map { it["$coinId:$currency"] }

    override suspend fun get(
        coinId: String,
        currency: String,
    ): CoinDetailsEntity? = store.value["$coinId:$currency"]

    override suspend fun upsert(details: CoinDetailsEntity) {
        store.value = store.value + ("${details.coinId}:${details.currency}" to details)
    }
}

internal class InMemoryPriceHistoryDao : PriceHistoryDao() {
    val store = MutableStateFlow<List<PriceHistoryEntity>>(emptyList())

    override fun observe(
        coinId: String,
        currency: String,
        range: String,
    ): Flow<List<PriceHistoryEntity>> =
        store.map { list ->
            list.filter { it.coinId == coinId && it.currency == currency && it.range == range }
        }

    override suspend fun clear(
        coinId: String,
        currency: String,
        range: String,
    ) {
        store.value =
            store.value.filterNot {
                it.coinId == coinId && it.currency == currency && it.range == range
            }
    }

    override suspend fun insertAll(points: List<PriceHistoryEntity>) {
        store.value = store.value + points
    }
}

internal class InMemoryCacheMetaDao : CacheMetaDao {
    val store = MutableStateFlow<Map<String, CacheMetaEntity>>(emptyMap())

    override fun observe(key: String): Flow<CacheMetaEntity?> = store.map { it[key] }

    override suspend fun get(key: String): CacheMetaEntity? = store.value[key]

    override suspend fun put(meta: CacheMetaEntity) {
        store.value = store.value + (meta.key to meta)
    }
}

/** Тестовая БД: подставляет фейковые DAO без Room-рантайма. */
class TestDatabase(
    val holding: HoldingDao = InMemoryHoldingDao(),
    val favorite: FavoriteDao = InMemoryFavoriteDao(),
    val market: MarketDao = InMemoryMarketDao(),
    val coinDetails: CoinDetailsDao = InMemoryCoinDetailsDao(),
    val priceHistory: PriceHistoryDao = InMemoryPriceHistoryDao(),
    val cacheMeta: CacheMetaDao = InMemoryCacheMetaDao(),
) : CryptoTrackDatabase() {
    override fun holdingDao(): HoldingDao = holding

    override fun favoriteDao(): FavoriteDao = favorite

    override fun marketDao(): MarketDao = market

    override fun coinDetailsDao(): CoinDetailsDao = coinDetails

    override fun priceHistoryDao(): PriceHistoryDao = priceHistory

    override fun cacheMetaDao(): CacheMetaDao = cacheMeta

    override fun clearAllTables() = Unit

    override fun createInvalidationTracker(): androidx.room.InvalidationTracker = throw UnsupportedOperationException(
        "not used in tests"
    )
}
