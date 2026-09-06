package com.cryptotrack.core.data.impl

import com.cryptotrack.core.common.time.TimeProvider
import com.cryptotrack.core.data.database.CryptoTrackDatabase
import com.cryptotrack.core.data.mapper.toCoin
import com.cryptotrack.core.data.repository.FavoritesRepository
import com.cryptotrack.core.data.database.entity.FavoriteEntity
import com.cryptotrack.core.model.Coin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    database: CryptoTrackDatabase,
    private val timeProvider: TimeProvider,
) : FavoritesRepository {

    private val favoriteDao = database.favoriteDao()
    private val marketDao = database.marketDao()

    override fun observeFavorites(currency: String): Flow<List<Coin>> =
        combine(
            favoriteDao.observeAll(),
            marketDao.observeAll(currency),
        ) { favorites, marketCoins ->
            favorites.mapNotNull { favorite ->
                marketCoins.find { it.coinId == favorite.coinId }?.toCoin()
            }
        }

    override fun observeFavoriteIds(): Flow<Set<String>> =
        favoriteDao.observeIds().map { it.toSet() }

    override fun observeIsFavorite(coinId: String): Flow<Boolean> =
        favoriteDao.observeIsFavorite(coinId)

    override suspend fun setFavorite(coinId: String, isFavorite: Boolean) {
        if (isFavorite) {
            favoriteDao.add(FavoriteEntity(coinId = coinId, addedAtMillis = timeProvider.nowMillis()))
        } else {
            favoriteDao.remove(coinId)
        }
    }
}
