package com.cryptotrack.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cryptotrack.core.model.AppSettings
import com.cryptotrack.core.model.Currency
import com.cryptotrack.core.model.RefreshInterval
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsStore by preferencesDataStore(name = "cryptotrack_settings")

/**
 * Настройки пользователя (Preferences-секция макета Settings) в DataStore.
 */
@Singleton
class SettingsDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private object Keys {
        val CURRENCY = stringPreferencesKey("currency")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val REFRESH_INTERVAL = stringPreferencesKey("refresh_interval")
    }

    val settings: Flow<AppSettings> = context.settingsStore.data.map { prefs ->
        AppSettings(
            currency = prefs[Keys.CURRENCY]?.toEnum<Currency>() ?: Currency.USD,
            isDarkMode = prefs[Keys.DARK_MODE] ?: false,
            refreshInterval = prefs[Keys.REFRESH_INTERVAL]?.toEnum<RefreshInterval>()
                ?: RefreshInterval.FIFTEEN_SECONDS,
        )
    }

    suspend fun setCurrency(currency: Currency) {
        context.settingsStore.edit { it[Keys.CURRENCY] = currency.name }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.settingsStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setRefreshInterval(interval: RefreshInterval) {
        context.settingsStore.edit { it[Keys.REFRESH_INTERVAL] = interval.name }
    }

    private inline fun <reified T : Enum<T>> String.toEnum(): T? =
        runCatching { enumValueOf<T>(this) }.getOrNull()
}
