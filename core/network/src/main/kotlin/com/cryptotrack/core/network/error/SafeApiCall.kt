package com.cryptotrack.core.network.error

import com.cryptotrack.core.common.result.AppError
import com.cryptotrack.core.common.result.Result
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Маппинг исключений сети в AppError (ADR-011). CancellationException всегда пробрасывается.
 * Здесь — единственное место, где перехват общих исключений оправдан (граница сети).
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> =
    try {
        Result.Success(call())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        Result.Failure(
            when (e.code()) {
                429 -> AppError.RateLimit
                404 -> AppError.NotFound
                else -> AppError.Http(e.code())
            },
        )
    } catch (e: SerializationException) {
        Result.Failure(AppError.Serialization)
    } catch (e: IOException) {
        Result.Failure(AppError.Network)
    } catch (e: Exception) {
        Result.Failure(AppError.Unknown(e.message))
    }
