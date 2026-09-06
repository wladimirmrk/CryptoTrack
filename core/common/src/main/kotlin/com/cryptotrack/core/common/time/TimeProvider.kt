package com.cryptotrack.core.common.time

/**
 * Время вводится через интерфейс, чтобы тесты контролировали TTL кэша и «Last updated».
 */
interface TimeProvider {
    fun nowMillis(): Long
}

class SystemTimeProvider : TimeProvider {
    override fun nowMillis(): Long = System.currentTimeMillis()
}
