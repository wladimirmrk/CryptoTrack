package com.cryptotrack.core.common.format

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/**
 * Форматирование денег/процентов по спецификации дизайн-системы «Apex Finance»:
 * tabular figures, JetBrains Mono рендерится компонентом; здесь — только текст.
 */
object MoneyFormat {

    private const val TRILLION = 1_000_000_000_000.0
    private const val BILLION = 1_000_000_000.0
    private const val MILLION = 1_000_000.0
    private const val THOUSAND = 1_000.0

    fun usd(amount: BigDecimal, locale: Locale = Locale.US): String {
        val value = amount.toDouble()
        val currency = NumberFormat.getCurrencyInstance(locale)
        return if (value != 0.0 && kotlin.math.abs(value) < 0.01) {
            // Микро-цены (BTT $0.0000012): значащие цифры без «группировки в ноль».
            currency.maximumFractionDigits = 8
            currency.minimumFractionDigits = 8
            currency.format(value).trimEnd('0').trimEnd(*localeSeparators(locale))
        } else {
            currency.maximumFractionDigits = 2
            currency.minimumFractionDigits = 2
            currency.format(value)
        }
    }

    /** Компактный формат больших значений: "$1.24 Trillion", "$34.5B", "$8.4M" (макеты Coin Details). */
    fun compact(amount: BigDecimal, locale: Locale = Locale.US): String {
        val value = amount.toDouble()
        val abs = kotlin.math.abs(value)
        val symbol = currencySymbol(locale)
        return when {
            abs >= TRILLION -> "${symbol}${format(value / TRILLION, locale)} Trillion"
            abs >= BILLION -> "${symbol}${format(value / BILLION, locale)}B"
            abs >= MILLION -> "${symbol}${format(value / MILLION, locale)}M"
            abs >= THOUSAND -> "${symbol}${format(value / THOUSAND, locale)}K"
            else -> usd(amount, locale)
        }
    }

    /** "+5.2%" / "-1.4%" / "0.0%" — знак и одна десятичная, как в макетах. */
    fun percent(change: Double): String {
        val sign = if (change > 0) "+" else ""
        return "${sign}%.1f%%".format(Locale.US, change)
    }

    private fun format(value: Double, locale: Locale): String {
        val nf = NumberFormat.getNumberInstance(locale)
        nf.maximumFractionDigits = 2
        nf.minimumFractionDigits = 1
        nf.roundingMode = RoundingMode.HALF_UP
        return nf.format(value)
    }

    private fun currencySymbol(locale: Locale): String =
        NumberFormat.getCurrencyInstance(locale).currency?.getSymbol(locale) ?: "$"

    private fun localeSeparators(locale: Locale): CharArray {
        val dfs = java.text.DecimalFormatSymbols(locale)
        return charArrayOf(dfs.decimalSeparator, dfs.groupingSeparator)
    }
}
