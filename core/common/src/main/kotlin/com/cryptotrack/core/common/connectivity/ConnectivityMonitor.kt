package com.cryptotrack.core.common.connectivity

import kotlinx.coroutines.flow.Flow

/**
 * Источник состояния сети. Реализация (ConnectivityManager) живёт в :app,
 * data-слой использует интерфейс для offline-first решений.
 */
interface ConnectivityMonitor {
    val isOnline: Flow<Boolean>
}
