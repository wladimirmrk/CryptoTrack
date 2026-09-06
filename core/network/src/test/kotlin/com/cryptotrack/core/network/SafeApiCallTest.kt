package com.cryptotrack.core.network

import com.cryptotrack.core.common.result.AppError
import com.cryptotrack.core.common.result.Result
import com.cryptotrack.core.network.error.safeApiCall
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class SafeApiCallTest {

    private fun httpException(code: Int): HttpException =
        HttpException(Response.error<Any>(code, ResponseBody.create(null, "")))

    @Test
    fun `http 429 maps to rate limit`() = runTest {
        val result = safeApiCall<Int> { throw httpException(429) }

        assertThat((result as Result.Failure).error).isEqualTo(AppError.RateLimit)
    }

    @Test
    fun `http 404 maps to not found`() = runTest {
        val result = safeApiCall<Int> { throw httpException(404) }

        assertThat((result as Result.Failure).error).isEqualTo(AppError.NotFound)
    }

    @Test
    fun `other http errors keep code`() = runTest {
        val result = safeApiCall<Int> { throw httpException(500) }

        assertThat((result as Result.Failure).error).isEqualTo(AppError.Http(500))
    }

    @Test
    fun `io errors map to network`() = runTest {
        val result = safeApiCall<Int> { throw IOException("offline") }

        assertThat((result as Result.Failure).error).isEqualTo(AppError.Network)
    }

    @Test
    fun `serialization errors map to serialization`() = runTest {
        val result = safeApiCall<Int> { throw SerializationException("bad json") }

        assertThat((result as Result.Failure).error).isEqualTo(AppError.Serialization)
    }

    @Test
    fun `cancellation is never swallowed`() = runTest {
        var thrown: CancellationException? = null
        try {
            safeApiCall<Int> { throw CancellationException("job cancelled") }
        } catch (e: CancellationException) {
            thrown = e
        }

        assertThat(thrown).isNotNull()
    }
}
