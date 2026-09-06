package com.cryptotrack.core.data.repository

import com.cryptotrack.core.model.AppSettings
import com.cryptotrack.core.model.Currency
import com.cryptotrack.core.model.RefreshInterval
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun setCurrency(currency: Currency)

    suspend fun setDarkMode(enabled: Boolean)

    suspend fun setRefreshInterval(interval: RefreshInterval)
}
