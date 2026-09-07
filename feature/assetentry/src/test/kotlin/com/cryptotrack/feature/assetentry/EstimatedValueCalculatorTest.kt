package com.cryptotrack.feature.assetentry

import com.cryptotrack.feature.assetentry.domain.EstimatedValueCalculator
import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import org.junit.Test

class EstimatedValueCalculatorTest {

    @Test
    fun `multiplies amount by current price`() {
        val value = EstimatedValueCalculator.compute(BigDecimal("0.25"), BigDecimal("64230"))
        assertThat(value).isEquivalentAccordingToCompareTo(BigDecimal("16057.5000"))
    }

    @Test
    fun `handles micro prices without rounding away digits`() {
        val value = EstimatedValueCalculator.compute(BigDecimal("1000"), BigDecimal("0.0000012"))
        assertThat(value).isEquivalentAccordingToCompareTo(BigDecimal("0.0012000"))
    }

    @Test
    fun `zero amount yields zero value`() {
        val value = EstimatedValueCalculator.compute(BigDecimal.ZERO, BigDecimal("100"))
        assertThat(value).isEquivalentAccordingToCompareTo(BigDecimal.ZERO)
    }

    @Test
    fun `missing amount yields null`() {
        assertThat(EstimatedValueCalculator.compute(null, BigDecimal("100"))).isNull()
    }

    @Test
    fun `missing price yields null`() {
        assertThat(EstimatedValueCalculator.compute(BigDecimal("0.25"), null)).isNull()
    }
}
