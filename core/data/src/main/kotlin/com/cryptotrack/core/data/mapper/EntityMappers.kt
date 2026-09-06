package com.cryptotrack.core.data.mapper

import com.cryptotrack.core.data.database.entity.CacheMetaEntity
import com.cryptotrack.core.data.database.entity.CoinDetailsEntity
import com.cryptotrack.core.data.database.entity.HoldingEntity
import com.cryptotrack.core.data.database.entity.MarketCoinEntity
import com.cryptotrack.core.data.database.entity.PriceHistoryEntity
import com.cryptotrack.core.model.Coin
import com.cryptotrack.core.model.CoinDetails
import com.cryptotrack.core.model.Holding
import com.cryptotrack.core.model.PricePoint
import java.math.BigDecimal

internal fun MarketCoinEntity.toCoin(): Coin = Coin(
    id = coinId,
    symbol = symbol,
    name = name,
    imageUrl = imageUrl,
    currentPrice = BigDecimal(currentPrice),
    priceChangePercentage24h = changePercent24h,
    marketCap = marketCap?.let(::BigDecimal),
    marketCapRank = marketCapRank,
    totalVolume = totalVolume?.let(::BigDecimal),
    high24h = high24h?.let(::BigDecimal),
    low24h = low24h?.let(::BigDecimal),
    circulatingSupply = circulatingSupply?.let(::BigDecimal),
)

internal fun Coin.toEntity(currency: String): MarketCoinEntity = MarketCoinEntity(
    coinId = id,
    currency = currency,
    symbol = symbol,
    name = name,
    imageUrl = imageUrl,
    currentPrice = currentPrice.toPlainString(),
    changePercent24h = priceChangePercentage24h,
    marketCap = marketCap?.toPlainString(),
    marketCapRank = marketCapRank,
    totalVolume = totalVolume?.toPlainString(),
    high24h = high24h?.toPlainString(),
    low24h = low24h?.toPlainString(),
    circulatingSupply = circulatingSupply?.toPlainString(),
)

internal fun CoinDetailsEntity.toCoinDetails(): CoinDetails = CoinDetails(
    id = coinId,
    symbol = symbol,
    name = name,
    imageUrl = imageUrl,
    description = description,
    currentPrice = BigDecimal(currentPrice),
    priceChangePercentage24h = changePercent24h,
    marketCap = marketCap?.let(::BigDecimal),
    marketCapRank = marketCapRank,
    totalVolume = totalVolume?.let(::BigDecimal),
    high24h = high24h?.let(::BigDecimal),
    low24h = low24h?.let(::BigDecimal),
    circulatingSupply = circulatingSupply?.let(::BigDecimal),
)

internal fun CoinDetails.toEntity(currency: String, fetchedAtMillis: Long): CoinDetailsEntity =
    CoinDetailsEntity(
        coinId = id,
        currency = currency,
        symbol = symbol,
        name = name,
        imageUrl = imageUrl,
        description = description,
        currentPrice = currentPrice.toPlainString(),
        changePercent24h = priceChangePercentage24h,
        marketCap = marketCap?.toPlainString(),
        marketCapRank = marketCapRank,
        totalVolume = totalVolume?.toPlainString(),
        high24h = high24h?.toPlainString(),
        low24h = low24h?.toPlainString(),
        circulatingSupply = circulatingSupply?.toPlainString(),
        fetchedAtMillis = fetchedAtMillis,
    )

internal fun PriceHistoryEntity.toPricePoint(): PricePoint =
    PricePoint(timestampMillis = timestampMillis, price = BigDecimal(price))

internal fun PricePoint.toEntity(coinId: String, currency: String, range: String): PriceHistoryEntity =
    PriceHistoryEntity(
        coinId = coinId,
        currency = currency,
        range = range,
        timestampMillis = timestampMillis,
        price = price.toPlainString(),
    )

internal fun HoldingEntity.toHolding(): Holding =
    Holding(coinId = coinId, amount = BigDecimal(amount), updatedAtMillis = updatedAtMillis)

internal fun Holding.toEntity(): HoldingEntity = HoldingEntity(
    coinId = coinId,
    amount = amount.toPlainString(),
    updatedAtMillis = updatedAtMillis,
)

internal fun cacheMeta(key: String, millis: Long): CacheMetaEntity =
    CacheMetaEntity(key = key, lastUpdatedMillis = millis)
