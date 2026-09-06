package com.cryptotrack.core.common.result

/**
 * Прикладной результат операции без исключений (ADR-011).
 * Имя совпадает с kotlin.Result намеренно: всегда импортируется явно.
 */
sealed interface Result<out T> {

    data class Success<T>(val data: T) : Result<T>

    data class Failure(val error: AppError) : Result<Nothing>

    fun getOrNull(): T? = (this as? Success<T>)?.data
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Failure -> this
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onFailure(action: (AppError) -> Unit): Result<T> {
    if (this is Result.Failure) action(error)
    return this
}
