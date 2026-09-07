package com.cryptotrack.feature.assetentry

import com.cryptotrack.feature.assetentry.domain.AssetAmountValidation
import com.cryptotrack.feature.assetentry.domain.AssetAmountValidator
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AssetAmountValidatorTest {

    @Test
    fun `accepts plain positive decimals`() {
        val valid = AssetAmountValidator.validate("0.25") as AssetAmountValidation.Valid
        assertThat(valid.amount).isEquivalentAccordingToCompareTo("0.25".toBigDecimal())

        val integer = AssetAmountValidator.validate("10") as AssetAmountValidation.Valid
        assertThat(integer.amount).isEquivalentAccordingToCompareTo("10".toBigDecimal())

        val micro = AssetAmountValidator.validate("0.0000012") as AssetAmountValidation.Valid
        assertThat(micro.amount).isEquivalentAccordingToCompareTo("0.0000012".toBigDecimal())

        val trailingDot = AssetAmountValidator.validate("5.") as AssetAmountValidation.Valid
        assertThat(trailingDot.amount).isEquivalentAccordingToCompareTo("5".toBigDecimal())
    }

    @Test
    fun `blank input is empty without error`() {
        assertThat(AssetAmountValidator.validate("")).isEqualTo(AssetAmountValidation.Empty)
        assertThat(AssetAmountValidator.validate("   ")).isEqualTo(AssetAmountValidation.Empty)
    }

    @Test
    fun `rejects non-decimal formats`() {
        assertThat(AssetAmountValidator.validate("abc")).isEqualTo(AssetAmountValidation.InvalidFormat)
        assertThat(AssetAmountValidator.validate("1,5")).isEqualTo(AssetAmountValidation.InvalidFormat)
        assertThat(AssetAmountValidator.validate("1e3")).isEqualTo(AssetAmountValidation.InvalidFormat)
        assertThat(AssetAmountValidator.validate("1..2")).isEqualTo(AssetAmountValidation.InvalidFormat)
        assertThat(AssetAmountValidator.validate("-5")).isEqualTo(AssetAmountValidation.InvalidFormat)
        assertThat(AssetAmountValidator.validate("1 000")).isEqualTo(AssetAmountValidation.InvalidFormat)
    }

    @Test
    fun `rejects zero and negative as not positive`() {
        assertThat(AssetAmountValidator.validate("0")).isEqualTo(AssetAmountValidation.NotPositive)
        assertThat(AssetAmountValidator.validate("0.0")).isEqualTo(AssetAmountValidation.NotPositive)
        assertThat(AssetAmountValidator.validate("0.000")).isEqualTo(AssetAmountValidation.NotPositive)
    }

    @Test
    fun `sanitize maps comma to dot`() {
        assertThat(AssetAmountValidator.sanitize("1,5")).isEqualTo("1.5")
        assertThat(AssetAmountValidator.sanitize(",5")).isEqualTo(".5")
    }

    @Test
    fun `sanitize strips foreign characters and extra dots`() {
        assertThat(AssetAmountValidator.sanitize("1a2b")).isEqualTo("12")
        assertThat(AssetAmountValidator.sanitize("1.2.3")).isEqualTo("1.23")
        assertThat(AssetAmountValidator.sanitize("abc")).isEmpty()
        assertThat(AssetAmountValidator.sanitize("-0.5")).isEqualTo("0.5")
        assertThat(AssetAmountValidator.sanitize("0.25")).isEqualTo("0.25")
    }
}
