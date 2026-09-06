package com.cryptotrack.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Добавляет demo-ключ CoinGecko, если он задан (local.properties → BuildConfig).
 * Публичные эндпоинты работают и без ключа.
 */
class AuthInterceptor @Inject constructor(private val apiKey: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = if (apiKey.isBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("x-cg-demo-api-key", apiKey)
                .build()
        }
        return chain.proceed(request)
    }
}
