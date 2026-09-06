package com.cryptotrack.core.common

import com.cryptotrack.core.common.result.AppError
import com.cryptotrack.core.common.result.Result
import com.cryptotrack.core.common.result.map
import com.cryptotrack.core.common.result.onFailure
import com.cryptotrack.core.common.result.onSuccess
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ResultTest {

    @Test
    fun `map transforms success data`() {
        val result: Result<Int> = Result.Success(1)

        val mapped = result.map { it + 1 }

        assertThat((mapped as Result.Success).data).isEqualTo(2)
    }

    @Test
    fun `map keeps failure untouched`() {
        val result: Result<Int> = Result.Failure(AppError.Network)

        val mapped = result.map { it + 1 }

        assertThat((mapped as Result.Failure).error).isEqualTo(AppError.Network)
    }

    @Test
    fun `onSuccess runs only for success`() {
        var captured: Int? = null

        Result.Success(7).onSuccess { captured = it }
        Result.Failure(AppError.Http(500)).onSuccess { captured = 0 }

        assertThat(captured).isEqualTo(7)
    }

    @Test
    fun `onFailure runs only for failure`() {
        var captured: AppError? = null

        Result.Failure(AppError.RateLimit).onFailure { captured = it }
        Result.Success(1).onFailure { captured = AppError.NotFound }

        assertThat(captured).isEqualTo(AppError.RateLimit)
    }

    @Test
    fun `getOrNull returns data or null`() {
        val failure: Result<Int> = Result.Failure(AppError.Serialization)

        assertThat(Result.Success(3).getOrNull()).isEqualTo(3)
        assertThat(failure.getOrNull()).isNull()
    }
}
