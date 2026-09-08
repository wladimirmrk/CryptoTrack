package com.cryptotrack.core.common.format

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/**
 * Форматирование денег/процентов по спецификации дизайн-системы «Apex Finance»:
 * tabular figures, JetBrains Mono рендерится компонентом; здесь — только текст.
 * Валюта передаётся символом (Currency.symbol из core:model — core:common не зависит
 * от core:model); конвенции US-локали сохранены (макеты: «$12,450.00», «$1.24 Trillion»).
 */
object MoneyFormat {
    private const val DEFAULT_SYMBOL = "$"
    private const val TRILLION = 1_000_000_000_000.0
    private const val BILLION = 1_000_000_000.0
    private const val MILLION = 1_000_000.0
    private const val THOUSAND = 1_000.0

    /** Сумма в валюте отображения: «$12,450.00», «€12,450.00»; микро-цены — «$0.0000012». */
    fun format(
        amount: BigDecimal,
        symbol: String = DEFAULT_SYMBOL,
    ): String {
        val value = amount.toDouble()
        val sign = if (value < 0) "-" else ""
        val number = NumberFormat.getNumberInstance(Locale.US)
        return if (value != 0.0 && kotlin.math.abs(value) < 0.01) {
            // Микро-цены (BTT $0.0000012): значащие цифры без «группировки в ноль».
            number.maximumFractionDigits = 8
            number.minimumFractionDigits = 8
            sign + symbol + trimSeparators(number.format(kotlin.math.abs(value)))
        } else {
            number.maximumFractionDigits = 2
            number.minimumFractionDigits = 2
            sign + symbol + number.format(kotlin.math.abs(value))
        }
    }

    /** Компактный формат больших значений: "$1.24 Trillion", "$34.5B", "$8.4M" (макеты Coin Details). */
    fun compact(
        amount: BigDecimal,
        symbol: String = DEFAULT_SYMBOL,
    ): String {
        val value = amount.toDouble()
        val abs = kotlin.math.abs(value)
        val sign = if (value < 0) "-" else ""
        return when {
            abs >= TRILLION -> "$sign$symbol${formatCompact(value / TRILLION)} Trillion"
            abs >= BILLION -> "$sign$symbol${formatCompact(value / BILLION)}B"
            abs >= MILLION -> "$sign$symbol${formatCompact(value / MILLION)}M"
            abs >= THOUSAND -> "$sign$symbol${formatCompact(value / THOUSAND)}K"
            else -> format(amount, symbol)
        }
    }

    /** "+5.2%" / "-1.4%" / "0.0%" — знак и одна десятичная, как в макетах. */
    fun percent(change: Double): String {
        val sign = if (change > 0) "+" else ""
        return sign + "%.1f%%".format(Locale.US, change)
    }

    private fun formatCompact(value: Double): String {
        val nf = NumberFormat.getNumberInstance(Locale.US)
        nf.maximumFractionDigits = 2
        nf.minimumFractionDigits = 1
        nf.roundingMode = RoundingMode.HALF_UP
        return nf.format(kotlin.math.abs(value))
    }

    private fun trimSeparators(value: String): String {
        val symbols = java.text.DecimalFormatSymbols(Locale.US)
        return value.trimEnd('0').trimEnd(symbols.decimalSeparator, symbols.groupingSeparator)
    }
}
