package com.cryptotrack.core.data.cache

import com.cryptotrack.core.model.ChartRange

/** Ключи cache_meta для «Last updated» offline-баннера. */
object CacheKeys {
    fun market(currency: String): String = "market:$currency"

    fun details(
        coinId: String,
        currency: String,
    ): String = "details:$coinId:$currency"

    fun history(
        coinId: String,
        currency: String,
        range: ChartRange,
    ): String = "history:$coinId:$currency:${range.name}"
}
