package com.cryptotrack.core.data.repository

import com.cryptotrack.core.model.Coin
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    /** Избранные коины с ценами из кэша маркет-списка. */
    fun observeFavorites(currency: String): Flow<List<Coin>>

    fun observeFavoriteIds(): Flow<Set<String>>

    fun observeIsFavorite(coinId: String): Flow<Boolean>

    suspend fun setFavorite(
        coinId: String,
        isFavorite: Boolean,
    )
}
