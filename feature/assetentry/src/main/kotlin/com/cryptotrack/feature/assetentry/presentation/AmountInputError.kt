package com.cryptotrack.feature.assetentry.presentation

import com.cryptotrack.feature.assetentry.domain.AssetAmountValidation
import java.math.BigDecimal

/** Inline amount field error (mapped to string resources on screen). */
enum class AmountInputError {
    InvalidFormat,
    NotPositive,
}

/** Maps a validation outcome to the inline error to show, null when input is fine. */
internal fun amountInputError(validation: AssetAmountValidation): AmountInputError? = when (validation) {
    AssetAmountValidation.InvalidFormat -> AmountInputError.InvalidFormat
    AssetAmountValidation.NotPositive -> AmountInputError.NotPositive
    else -> null
}

/**
 * Amount used for the live Estimated Total Value: valid input as parsed, empty input
 * as zero (the mockup's "$0.00" placeholder), invalid input as null (renders a dash).
 */
internal fun estimatedAmount(validation: AssetAmountValidation): BigDecimal? = when (validation) {
    is AssetAmountValidation.Valid -> validation.amount
    AssetAmountValidation.Empty -> BigDecimal.ZERO
    else -> null
}
