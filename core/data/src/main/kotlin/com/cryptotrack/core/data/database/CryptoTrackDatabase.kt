package com.cryptotrack.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
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

@Database(
    entities = [
        HoldingEntity::class,
        FavoriteEntity::class,
        MarketCoinEntity::class,
        CoinDetailsEntity::class,
        PriceHistoryEntity::class,
        CacheMetaEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class CryptoTrackDatabase : RoomDatabase() {
    abstract fun holdingDao(): HoldingDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun marketDao(): MarketDao
    abstract fun coinDetailsDao(): CoinDetailsDao
    abstract fun priceHistoryDao(): PriceHistoryDao
    abstract fun cacheMetaDao(): CacheMetaDao

    companion object {
        const val NAME = "cryptotrack.db"
    }
}
