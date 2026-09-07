package com.cryptotrack.feature.assetentry.domain

import java.math.BigDecimal

/** Why an amount input was rejected (mapped to inline UI errors in presentation). */
enum class AssetAmountError {
    InvalidFormat,
    NotPositive,
}

/** Validation outcome for the "Amount Owned" / "Holdings Amount" input. */
sealed interface AssetAmountValidation {
    data class Valid(val amount: BigDecimal) : AssetAmountValidation

    /** Input is blank — treated as "not entered yet", no inline error. */
    data object Empty : AssetAmountValidation

    /** Not a plain decimal number (letters, commas, exponent notation, multiple dots). */
    data object InvalidFormat : AssetAmountValidation

    /** Parses as a number but is zero (negative never reaches here after sanitize). */
    data object NotPositive : AssetAmountValidation
}

/**
 * Parses and validates the asset amount input (Phase 8): non-empty, decimal with a dot
 * separator, strictly positive. Zero and negative amounts are rejected at form level;
 * the repository does not duplicate the check (ADR-017).
 */
object AssetAmountValidator {

    private val decimalPattern = Regex("^(\\d+(\\.\\d*)?|\\.\\d+)$")

    fun validate(raw: String): AssetAmountValidation {
        val text = raw.trim()
        val amount = when {
            text.isEmpty() -> null
            else -> text.takeIf { decimalPattern.matches(it) }?.let(::BigDecimal)
        }
        return when {
            text.isEmpty() -> AssetAmountValidation.Empty
            amount == null -> AssetAmountValidation.InvalidFormat
            amount.signum() > 0 -> AssetAmountValidation.Valid(amount)
            else -> AssetAmountValidation.NotPositive
        }
    }

    /**
     * Soft input filtering for the text field: comma falls back to a dot, anything but
     * digits and a single dot is dropped. Kept in domain so the Add sheet and the Edit
     * screen behave identically and stay unit-testable.
     */
    fun sanitize(raw: String): String {
        val normalized = raw.replace(',', '.')
        val builder = StringBuilder(normalized.length)
        var dotSeen = false
        for (char in normalized) {
            when {
                char.isDigit() -> builder.append(char)
                char == '.' && !dotSeen -> {
                    dotSeen = true
                    builder.append(char)
                }
            }
        }
        return builder.toString()
    }
}
