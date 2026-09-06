package com.cryptotrack.core.common

import com.cryptotrack.core.common.format.MoneyFormat
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.math.BigDecimal

class MoneyFormatTest {
    @Test
    fun `usd formats grouped two decimals`() {
        assertThat(MoneyFormat.usd(BigDecimal("12450"))).isEqualTo("$12,450.00")
        assertThat(MoneyFormat.usd(BigDecimal("8400.5"))).isEqualTo("$8,400.50")
    }

    @Test
    fun `usd keeps micro prices significant`() {
        assertThat(MoneyFormat.usd(BigDecimal("0.0000012"))).isEqualTo("$0.0000012")
        assertThat(MoneyFormat.usd(BigDecimal("0.005"))).isEqualTo("$0.005")
    }

    @Test
    fun `usd zero formats as zero dollars`() {
        assertThat(MoneyFormat.usd(BigDecimal.ZERO)).isEqualTo("$0.00")
    }

    @Test
    fun `compact uses trillion suffix like mockups`() {
        assertThat(MoneyFormat.compact(BigDecimal("1240000000000"))).isEqualTo("$1.24 Trillion")
    }

    @Test
    fun `compact uses billion and million suffixes`() {
        assertThat(MoneyFormat.compact(BigDecimal("34500000000"))).isEqualTo("$34.5B")
        assertThat(MoneyFormat.compact(BigDecimal("8400000"))).isEqualTo("$8.4M")
        assertThat(MoneyFormat.compact(BigDecimal("12500"))).isEqualTo("$12.5K")
    }

    @Test
    fun `compact falls back to usd for small values`() {
        assertThat(MoneyFormat.compact(BigDecimal("850"))).isEqualTo("$850.00")
    }

    @Test
    fun `percent adds sign and one decimal`() {
        assertThat(MoneyFormat.percent(5.23)).isEqualTo("+5.2%")
        assertThat(MoneyFormat.percent(-1.44)).isEqualTo("-1.4%")
        assertThat(MoneyFormat.percent(0.0)).isEqualTo("0.0%")
        assertThat(MoneyFormat.percent(12.85)).isEqualTo("+12.9%")
    }
}
