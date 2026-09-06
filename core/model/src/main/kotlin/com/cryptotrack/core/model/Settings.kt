package com.cryptotrack.core.model

/** Валюта отображения (макет Settings: «Base display currency»). */
enum class Currency(val apiCode: String, val symbol: String) {
    USD("usd", "$"),
    EUR("eur", "€"),
    GBP("gbp", "£"),
    RUB("rub", "₽"),
    JPY("jpy", "¥"),
}

/** Интервал автообновления маркет-данных в foreground (макет Settings: «Market data sync rate» «15s»). */
enum class RefreshInterval(val millis: Long, val label: String) {
    FIFTEEN_SECONDS(millis = 15_000L, label = "15s"),
    THIRTY_SECONDS(millis = 30_000L, label = "30s"),
    ONE_MINUTE(millis = 60_000L, label = "1m"),
    FIVE_MINUTES(millis = 300_000L, label = "5m"),
}

/** Пользовательские настройки (Preferences-секция макета Settings). */
data class AppSettings(
    val currency: Currency = Currency.USD,
    val isDarkMode: Boolean = false,
    val refreshInterval: RefreshInterval = RefreshInterval.FIFTEEN_SECONDS,
)
