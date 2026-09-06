package com.cryptotrack.core.data.impl

import com.cryptotrack.core.data.repository.SettingsRepository
import com.cryptotrack.core.datastore.SettingsDataSource
import com.cryptotrack.core.model.AppSettings
import com.cryptotrack.core.model.Currency
import com.cryptotrack.core.model.RefreshInterval
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataSource: SettingsDataSource,
) : SettingsRepository {

    override val settings: Flow<AppSettings> = dataSource.settings

    override suspend fun setCurrency(currency: Currency) = dataSource.setCurrency(currency)

    override suspend fun setDarkMode(enabled: Boolean) = dataSource.setDarkMode(enabled)

    override suspend fun setRefreshInterval(interval: RefreshInterval) =
        dataSource.setRefreshInterval(interval)
}
