package com.cryptotrack.core.common.logging

/**
 * Логгер-интерфейс (без внешних зависимостей, ADR-011).
 * Logcat-реализация живёт в :app.
 */
interface Logger {
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}
