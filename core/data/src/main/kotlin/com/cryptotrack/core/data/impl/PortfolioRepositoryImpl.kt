package com.cryptotrack.core.data.impl

import com.cryptotrack.core.common.time.TimeProvider
import com.cryptotrack.core.data.database.CryptoTrackDatabase
import com.cryptotrack.core.data.mapper.toEntity
import com.cryptotrack.core.data.mapper.toHolding
import com.cryptotrack.core.data.repository.PortfolioRepository
import com.cryptotrack.core.model.Holding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PortfolioRepositoryImpl @Inject constructor(
    database: CryptoTrackDatabase,
    private val timeProvider: TimeProvider,
) : PortfolioRepository {

    private val holdingDao = database.holdingDao()

    override fun observeHoldings(): Flow<List<Holding>> =
        holdingDao.observeAll().map { list -> list.map { it.toHolding() } }

    override fun observeHolding(coinId: String): Flow<Holding?> =
        holdingDao.observe(coinId).map { it?.toHolding() }

    override suspend fun saveHolding(coinId: String, amount: BigDecimal) {
        // Повторное добавление того же коина суммируется (ADR-013).
        val existing = holdingDao.get(coinId)
        val total = existing?.amount?.let { BigDecimal(it).add(amount) } ?: amount
        holdingDao.upsert(
            Holding(coinId = coinId, amount = total, updatedAtMillis = timeProvider.nowMillis()).toEntity(),
        )
    }

    override suspend fun setHoldingAmount(coinId: String, amount: BigDecimal) {
        holdingDao.upsert(
            Holding(coinId = coinId, amount = amount, updatedAtMillis = timeProvider.nowMillis()).toEntity(),
        )
    }

    override suspend fun removeHolding(coinId: String) {
        holdingDao.delete(coinId)
    }
}
