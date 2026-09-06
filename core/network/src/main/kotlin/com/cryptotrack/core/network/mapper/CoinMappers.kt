package com.cryptotrack.core.network.mapper

import com.cryptotrack.core.model.Coin
import com.cryptotrack.core.model.CoinDetails
import com.cryptotrack.core.model.PricePoint
import com.cryptotrack.core.network.dto.CoinDetailsDto
import com.cryptotrack.core.network.dto.MarketChartDto
import com.cryptotrack.core.network.dto.MarketCoinDto
import com.cryptotrack.core.network.dto.SearchCoinDto
import java.math.BigDecimal

private fun Double?.toBigDecimalOrNull(): BigDecimal? = this?.let { BigDecimal.valueOf(it) }

fun MarketCoinDto.toCoin(): Coin =
    Coin(
        id = id,
        symbol = symbol.uppercase(),
        name = name,
        imageUrl = image,
        currentPrice = current_price.toBigDecimalOrNull() ?: BigDecimal.ZERO,
        priceChangePercentage24h = priceChangePercentage24h ?: 0.0,
        marketCap = market_cap.toBigDecimalOrNull(),
        marketCapRank = market_cap_rank,
        totalVolume = total_volume.toBigDecimalOrNull(),
        high24h = high_24h.toBigDecimalOrNull(),
        low24h = low_24h.toBigDecimalOrNull(),
        circulatingSupply = circulating_supply.toBigDecimalOrNull(),
    )

fun CoinDetailsDto.toCoinDetails(currency: String): CoinDetails {
    val marketData = market_data
    return CoinDetails(
        id = id,
        symbol = symbol.uppercase(),
        name = name,
        imageUrl = image?.large,
        description = description?.en.orEmpty(),
        currentPrice =
            marketData
                ?.current_price
                ?.get(currency)
                ?.let { BigDecimal.valueOf(it) } ?: BigDecimal.ZERO,
        priceChangePercentage24h = marketData?.priceChangePercentage24h ?: 0.0,
        marketCap = marketData?.market_cap?.get(currency)?.let { BigDecimal.valueOf(it) },
        marketCapRank = marketData?.marketCapRank,
        totalVolume = marketData?.total_volume?.get(currency)?.let { BigDecimal.valueOf(it) },
        high24h = marketData?.high_24h?.get(currency)?.let { BigDecimal.valueOf(it) },
        low24h = marketData?.low_24h?.get(currency)?.let { BigDecimal.valueOf(it) },
        circulatingSupply = marketData?.circulating_supply?.toBigDecimalOrNull(),
    )
}

fun MarketChartDto.toPricePoints(): List<PricePoint> =
    prices
        .filter { it.size >= 2 }
        .map { PricePoint(timestampMillis = it[0].toLong(), price = BigDecimal.valueOf(it[1])) }

fun SearchCoinDto.toCoin(currentPrice: BigDecimal = BigDecimal.ZERO): Coin =
    Coin(
        id = id,
        symbol = symbol.uppercase(),
        name = name,
        imageUrl = large ?: thumb,
        currentPrice = currentPrice,
        priceChangePercentage24h = 0.0,
        marketCap = null,
        marketCapRank = marketCapRank,
        totalVolume = null,
        high24h = null,
        low24h = null,
        circulatingSupply = null,
    )
