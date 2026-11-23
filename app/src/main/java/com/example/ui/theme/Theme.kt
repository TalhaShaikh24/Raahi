package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RaahiLightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = Color.White,
    primaryContainer = AppColors.PrimarySoft,
    onPrimaryContainer = AppColors.PrimaryOnSoft,
    secondary = AppColors.PrimaryDark,
    onSecondary = Color.White,
    secondaryContainer = AppColors.PrimarySoft,
    onSecondaryContainer = AppColors.PrimaryOnSoft,
    tertiary = AppColors.Accent,
    onTertiary = AppColors.TextPrimary,
    tertiaryContainer = AppColors.AccentSoft,
    onTertiaryContainer = AppColors.AccentOnSoft,
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.SurfaceSubtle,
    onSurfaceVariant = AppColors.TextSecondary,
    outline = AppColors.Border,
    outlineVariant = AppColors.Border,
    error = AppColors.Error,
    onError = Color.White,
    errorContainer = AppColors.SosBg,
    onErrorContainer = AppColors.Error,
)

val RaahiShapes = Shapes(
    small = RoundedCornerShape(AppSpacing.radiusInput),
    medium = RoundedCornerShape(AppSpacing.radiusButton),
    large = RoundedCornerShape(AppSpacing.radiusCard),
    extraLarge = RoundedCornerShape(AppSpacing.radiusSheet),
)

@Composable
fun RaahiTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = RaahiLightColorScheme,
        typography = RaahiTypography,
        shapes = RaahiShapes,
        content = content,
    )
}
