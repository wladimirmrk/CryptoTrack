package com.cryptotrack.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Light-схема — точные токены дизайн-системы «Apex Finance» (docs/design-analysis.md §1.2).
private val ApexLightColors = lightColorScheme(
    primary = Color(0xFF003EC7),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF0052FF),
    onPrimaryContainer = Color(0xFFDFE3FF),
    inversePrimary = Color(0xFFB7C4FF),
    secondary = Color(0xFF505F76),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD0E1FB),
    onSecondaryContainer = Color(0xFF54647A),
    tertiary = Color(0xFF952200),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBF3003),
    onTertiaryContainer = Color(0xFFFFDDD5),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFFBF8FF),
    onBackground = Color(0xFF191B25),
    surface = Color(0xFFFBF8FF),
    onSurface = Color(0xFF191B25),
    surfaceVariant = Color(0xFFE1E1EF),
    onSurfaceVariant = Color(0xFF434656),
    outline = Color(0xFF737688),
    outlineVariant = Color(0xFFC3C5D9),
    inverseSurface = Color(0xFF2E303A),
    inverseOnSurface = Color(0xFFF0EFFE),
    surfaceTint = Color(0xFF004CED),
    surfaceDim = Color(0xFFD9D9E7),
    surfaceBright = Color(0xFFFBF8FF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F2FF),
    surfaceContainer = Color(0xFFEDEDFB),
    surfaceContainerHigh = Color(0xFFE7E7F5),
    surfaceContainerHighest = Color(0xFFE1E1EF),
)

// Dark-схема сгенерирована из того же seed-цвета (дизайн light-only, ADR-012).
private val ApexDarkColors = darkColorScheme(
    primary = Color(0xFFB7C4FF),
    onPrimary = Color(0xFF00258E),
    primaryContainer = Color(0xFF0052FF),
    onPrimaryContainer = Color(0xFFDFE3FF),
    inversePrimary = Color(0xFF003EC7),
    secondary = Color(0xFFB8C8E1),
    onSecondary = Color(0xFF223043),
    secondaryContainer = Color(0xFF38485D),
    onSecondaryContainer = Color(0xFFD3E4FE),
    tertiary = Color(0xFFFFB4A1),
    onTertiary = Color(0xFF5F1600),
    tertiaryContainer = Color(0xFF7B2B00),
    onTertiaryContainer = Color(0xFFFFDBD2),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF121318),
    onBackground = Color(0xFFE4E1EC),
    surface = Color(0xFF121318),
    onSurface = Color(0xFFE4E1EC),
    surfaceVariant = Color(0xFF434656),
    onSurfaceVariant = Color(0xFFC3C5D9),
    outline = Color(0xFF8E90A3),
    outlineVariant = Color(0xFF434656),
    inverseSurface = Color(0xFFE4E1EC),
    inverseOnSurface = Color(0xFF2F303A),
    surfaceTint = Color(0xFFB7C4FF),
    surfaceDim = Color(0xFF121318),
    surfaceBright = Color(0xFF383943),
    surfaceContainerLowest = Color(0xFF0D0E13),
    surfaceContainerLow = Color(0xFF1A1B23),
    surfaceContainer = Color(0xFF1E2027),
    surfaceContainerHigh = Color(0xFF292A32),
    surfaceContainerHighest = Color(0xFF34353D),
)

/** Additional semantic colors of price movement (assumption A-11). */
@Immutable
data class ApexPriceColors(
    val up: Color,
    val down: Color,
    // Badge/containers behind price-change texts (A-11 success container; dark derived).
    val upContainer: Color,
    val downContainer: Color,
)

private val LightPriceColors = ApexPriceColors(
    up = Color(0xFF15803D),
    down = Color(0xFFBA1A1A),
    upContainer = Color(0xFFDCFCE7),
    downContainer = Color(0xFFFFDAD6),
)

private val DarkPriceColors = ApexPriceColors(
    up = Color(0xFF7AD18F),
    down = Color(0xFFFFB4AB),
    upContainer = Color(0xFF1B4A2E),
    downContainer = Color(0xFF93000A),
)

val LocalApexPriceColors = staticCompositionLocalOf { LightPriceColors }

val ColorScheme.priceColors: ApexPriceColors
    @Composable get() = LocalApexPriceColors.current

/**
 * Extended tokens without an M3 counterpart (Phase 11 dark-theme revision, ADR-012):
 * card fill of Level-1 cards and the allocation donut segment palette.
 */
@Immutable
data class ApexExtraColors(
    val cardContainer: Color,
    val donutSegments: List<Color>,
)

// Light donut palette: brand blues + secondary gray + burnt orange (Portfolio Overview mockup).
private val LightExtraColors = ApexExtraColors(
    cardContainer = Color(0xFFFFFFFF), // design §1.4: white card fill on light
    donutSegments = listOf(
        Color(0xFF0052FF),
        Color(0xFF003EC7),
        Color(0xFF7B9BFF),
        Color(0xFF505F76),
        Color(0xFFB7C4FF),
        Color(0xFF952200),
    ),
)

// Dark: lightened tints of the same hues, ordered bright/mid alternating so the
// neighbouring segments stay distinguishable on the near-black background.
private val DarkExtraColors = ApexExtraColors(
    cardContainer = Color(0xFF1A1B23), // one tonal step above background
    donutSegments = listOf(
        Color(0xFF7B9BFF),
        Color(0xFF9AA8C7),
        Color(0xFF4D8CFF),
        Color(0xFFE08D5A),
        Color(0xFFB7C4FF),
        Color(0xFFFFB59A),
    ),
)

val LocalApexExtraColors = staticCompositionLocalOf { LightExtraColors }

val ColorScheme.apexExtra: ApexExtraColors
    @Composable get() = LocalApexExtraColors.current

@Composable
fun CryptoTrackTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) ApexDarkColors else ApexLightColors,
        typography = ApexTypography,
        shapes = ApexShapes,
    ) {
        CompositionLocalProvider(
            LocalApexPriceColors provides if (darkTheme) DarkPriceColors else LightPriceColors,
            LocalApexExtraColors provides if (darkTheme) DarkExtraColors else LightExtraColors,
            content = content,
        )
    }
}
