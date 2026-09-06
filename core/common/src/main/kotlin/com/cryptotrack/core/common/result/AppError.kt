package com.cryptotrack.core.common.result

/**
 * Ошибка прикладного уровня. Создаётся в data-слоях (маппинг исключений),
 * потребляется presentation-слоем для выбора UI-состояния.
 */
sealed interface AppError {
    data object Network : AppError

    data class Http(val code: Int) : AppError

    data object RateLimit : AppError

    data object NotFound : AppError

    data object Serialization : AppError

    data class Unknown(val message: String?) : AppError
}
