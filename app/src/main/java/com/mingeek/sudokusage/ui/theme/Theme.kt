package com.mingeek.sudokusage.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.mingeek.sudokusage.data.preferences.ThemePalette

private val SageLight = lightColorScheme(
    primary = SageIndigo500, onPrimary = SageMist,
    primaryContainer = SageIndigo100, onPrimaryContainer = SageIndigo900,
    secondary = SageAmber700, onSecondary = SageMist,
    secondaryContainer = SageAmber300, onSecondaryContainer = SageAmber900,
    tertiary = SageLavender500, onTertiary = SageMist,
    background = SageMist, onBackground = SageInk,
    surface = SageMist, onSurface = SageInk,
    surfaceVariant = SageStone, onSurfaceVariant = SageStoneDark,
    error = SageError, onError = SageMist,
)

private val SageDark = darkColorScheme(
    primary = SageIndigo300, onPrimary = SageIndigo900,
    primaryContainer = SageIndigo700, onPrimaryContainer = SageIndigo100,
    secondary = SageAmber500, onSecondary = SageAmber900,
    secondaryContainer = SageAmber700, onSecondaryContainer = SageAmber300,
    tertiary = SageLavender300, onTertiary = SageIndigo900,
    background = SageIndigo900, onBackground = SageStone,
    surface = SageIndigo900, onSurface = SageStone,
    surfaceVariant = SageStoneDark, onSurfaceVariant = SageStone,
    error = SageErrorDark, onError = SageIndigo900,
)

private val ForestLight = lightColorScheme(
    primary = ForestGreen500, onPrimary = ForestMist,
    primaryContainer = ForestGreen100, onPrimaryContainer = ForestGreen900,
    secondary = ForestCream500, onSecondary = ForestCream900,
    secondaryContainer = ForestCream300, onSecondaryContainer = ForestCream900,
    tertiary = ForestGreen500, onTertiary = ForestMist,
    background = ForestMist, onBackground = SageInk,
    surface = ForestMist, onSurface = SageInk,
    surfaceVariant = ForestStone, onSurfaceVariant = ForestStoneDark,
    error = SageError, onError = ForestMist,
)

private val ForestDark = darkColorScheme(
    primary = ForestGreen300, onPrimary = ForestGreen900,
    primaryContainer = ForestGreen700, onPrimaryContainer = ForestGreen100,
    secondary = ForestCream500, onSecondary = ForestCream900,
    secondaryContainer = ForestGreen700, onSecondaryContainer = ForestCream300,
    tertiary = ForestGreen300, onTertiary = ForestGreen900,
    background = ForestGreen900, onBackground = ForestStone,
    surface = ForestGreen900, onSurface = ForestStone,
    surfaceVariant = ForestStoneDark, onSurfaceVariant = ForestStone,
    error = SageErrorDark, onError = ForestGreen900,
)

private val OceanLight = lightColorScheme(
    primary = OceanNavy500, onPrimary = OceanMist,
    primaryContainer = OceanNavy100, onPrimaryContainer = OceanNavy900,
    secondary = OceanCoral500, onSecondary = OceanMist,
    secondaryContainer = OceanCoral300, onSecondaryContainer = OceanCoral900,
    tertiary = OceanNavy500, onTertiary = OceanMist,
    background = OceanMist, onBackground = SageInk,
    surface = OceanMist, onSurface = SageInk,
    surfaceVariant = OceanStone, onSurfaceVariant = OceanStoneDark,
    error = SageError, onError = OceanMist,
)

private val OceanDark = darkColorScheme(
    primary = OceanNavy300, onPrimary = OceanNavy900,
    primaryContainer = OceanNavy700, onPrimaryContainer = OceanNavy100,
    secondary = OceanCoral500, onSecondary = OceanCoral900,
    secondaryContainer = OceanNavy700, onSecondaryContainer = OceanCoral300,
    tertiary = OceanNavy300, onTertiary = OceanNavy900,
    background = OceanNavy900, onBackground = OceanStone,
    surface = OceanNavy900, onSurface = OceanStone,
    surfaceVariant = OceanStoneDark, onSurfaceVariant = OceanStone,
    error = SageErrorDark, onError = OceanNavy900,
)

private val SunsetLight = lightColorScheme(
    primary = SunsetOrange500, onPrimary = SunsetMist,
    primaryContainer = SunsetOrange100, onPrimaryContainer = SunsetOrange900,
    secondary = SunsetMagenta500, onSecondary = SunsetMist,
    secondaryContainer = SunsetMagenta300, onSecondaryContainer = SunsetMagenta900,
    tertiary = SunsetOrange500, onTertiary = SunsetMist,
    background = SunsetMist, onBackground = SageInk,
    surface = SunsetMist, onSurface = SageInk,
    surfaceVariant = SunsetStone, onSurfaceVariant = SunsetStoneDark,
    error = SageError, onError = SunsetMist,
)

private val SunsetDark = darkColorScheme(
    primary = SunsetOrange300, onPrimary = SunsetOrange900,
    primaryContainer = SunsetOrange700, onPrimaryContainer = SunsetOrange100,
    secondary = SunsetMagenta500, onSecondary = SunsetMagenta900,
    secondaryContainer = SunsetOrange700, onSecondaryContainer = SunsetMagenta300,
    tertiary = SunsetOrange300, onTertiary = SunsetOrange900,
    background = SunsetOrange900, onBackground = SunsetStone,
    surface = SunsetOrange900, onSurface = SunsetStone,
    surfaceVariant = SunsetStoneDark, onSurfaceVariant = SunsetStone,
    error = SageErrorDark, onError = SunsetOrange900,
)

private val LavenderLight = lightColorScheme(
    primary = LavenderPurple500, onPrimary = LavenderMist,
    primaryContainer = LavenderPurple100, onPrimaryContainer = LavenderPurple900,
    secondary = LavenderMint500, onSecondary = LavenderMist,
    secondaryContainer = LavenderMint300, onSecondaryContainer = LavenderMint900,
    tertiary = LavenderPurple500, onTertiary = LavenderMist,
    background = LavenderMist, onBackground = SageInk,
    surface = LavenderMist, onSurface = SageInk,
    surfaceVariant = LavenderStone, onSurfaceVariant = LavenderStoneDark,
    error = SageError, onError = LavenderMist,
)

private val LavenderDark = darkColorScheme(
    primary = LavenderPurple300, onPrimary = LavenderPurple900,
    primaryContainer = LavenderPurple700, onPrimaryContainer = LavenderPurple100,
    secondary = LavenderMint500, onSecondary = LavenderMint900,
    secondaryContainer = LavenderPurple700, onSecondaryContainer = LavenderMint300,
    tertiary = LavenderPurple300, onTertiary = LavenderPurple900,
    background = LavenderPurple900, onBackground = LavenderStone,
    surface = LavenderPurple900, onSurface = LavenderStone,
    surfaceVariant = LavenderStoneDark, onSurfaceVariant = LavenderStone,
    error = SageErrorDark, onError = LavenderPurple900,
)

private fun amoledOf(darkScheme: ColorScheme): ColorScheme = darkScheme.copy(
    background = AmoledBlack,
    surface = AmoledBlack,
    surfaceVariant = AmoledSurface,
    primaryContainer = AmoledSurface,
)

@Composable
fun SudokuSageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    amoled: Boolean = false,
    palette: ThemePalette = ThemePalette.Sage,
    content: @Composable () -> Unit,
) {
    val (light, dark) = when (palette) {
        ThemePalette.Sage -> SageLight to SageDark
        ThemePalette.Forest -> ForestLight to ForestDark
        ThemePalette.Ocean -> OceanLight to OceanDark
        ThemePalette.Sunset -> SunsetLight to SunsetDark
        ThemePalette.Lavender -> LavenderLight to LavenderDark
    }
    val colorScheme = when {
        amoled -> amoledOf(dark)
        darkTheme -> dark
        else -> light
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
