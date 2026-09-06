package com.cryptotrack.core.data.repository

import com.cryptotrack.core.model.Holding
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

/**
 * Портфель — только локальное хранилище (ADR-013).
 * saveHolding суммирует с существующим холдингом; setHoldingAmount задаёт точное значение.
 */
interface PortfolioRepository {

    fun observeHoldings(): Flow<List<Holding>>

    fun observeHolding(coinId: String): Flow<Holding?>

    suspend fun saveHolding(coinId: String, amount: BigDecimal)

    suspend fun setHoldingAmount(coinId: String, amount: BigDecimal)

    suspend fun removeHolding(coinId: String)
}
