package com.cryptotrack.core.common

import com.cryptotrack.core.common.format.MoneyFormat
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.math.BigDecimal

class MoneyFormatTest {
    @Test
    fun `format groups two decimals with default dollar symbol`() {
        assertThat(MoneyFormat.format(BigDecimal("12450"))).isEqualTo("$12,450.00")
        assertThat(MoneyFormat.format(BigDecimal("8400.5"))).isEqualTo("$8,400.50")
    }

    @Test
    fun `format keeps micro prices significant`() {
        assertThat(MoneyFormat.format(BigDecimal("0.0000012"))).isEqualTo("$0.0000012")
        assertThat(MoneyFormat.format(BigDecimal("0.005"))).isEqualTo("$0.005")
    }

    @Test
    fun `format zero formats as zero dollars`() {
        assertThat(MoneyFormat.format(BigDecimal.ZERO)).isEqualTo("$0.00")
    }

    @Test
    fun `format negatives keep sign before symbol`() {
        assertThat(MoneyFormat.format(BigDecimal("-12450.5"))).isEqualTo("-$12,450.50")
    }

    @Test
    fun `format uses the display currency symbol`() {
        assertThat(MoneyFormat.format(BigDecimal("12450"), "€")).isEqualTo("€12,450.00")
        assertThat(MoneyFormat.format(BigDecimal("0.0000012"), "₽")).isEqualTo("₽0.0000012")
        assertThat(MoneyFormat.format(BigDecimal("-12450.5"), "£")).isEqualTo("-£12,450.50")
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
    fun `compact falls back to format for small values`() {
        assertThat(MoneyFormat.compact(BigDecimal("850"))).isEqualTo("$850.00")
    }

    @Test
    fun `compact uses the display currency symbol`() {
        assertThat(MoneyFormat.compact(BigDecimal("1240000000000"), "€")).isEqualTo("€1.24 Trillion")
        assertThat(MoneyFormat.compact(BigDecimal("34500000000"), "¥")).isEqualTo("¥34.5B")
    }

    @Test
    fun `percent adds sign and one decimal`() {
        assertThat(MoneyFormat.percent(5.23)).isEqualTo("+5.2%")
        assertThat(MoneyFormat.percent(-1.44)).isEqualTo("-1.4%")
        assertThat(MoneyFormat.percent(0.0)).isEqualTo("0.0%")
        assertThat(MoneyFormat.percent(12.85)).isEqualTo("+12.9%")
    }
}
