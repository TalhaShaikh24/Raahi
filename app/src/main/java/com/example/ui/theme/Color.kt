package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object AppColors {
    val Primary = Color(0xFF0E7C61)
    val PrimaryDark = Color(0xFF0A5C48)
    val PrimarySoft = Color(0xFFE7F5F1)
    val PrimaryOnSoft = Color(0xFF0A5C48)

    // Amber used strictly for price highlights / ratings
    val Accent = Color(0xFFF59E0B)
    val AccentSoft = Color(0xFFFEF3C7)
    val AccentOnSoft = Color(0xFF92400E)

    val Background = Color(0xFFF6F7F8)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceSubtle = Color(0xFFEEF0F2)

    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF6B7280)
    val Border = Color(0xFFE5E7EB)

    val VerifiedBg = Color(0xFFE7F6EC)
    val VerifiedText = Color(0xFF16A34A)
    val UnverifiedBg = Color(0xFFEEF0F2)
    val UnverifiedText = Color(0xFF6B7280)

    val Error = Color(0xFFDC2626)
    val SosBg = Color(0xFFFEF2F2)

    val FemaleOnlyBg = Color(0xFFFDF2F8)
    val FemaleOnlyText = Color(0xFFBE185D)

    // Brand extensions for gradients, hero surfaces and highlights
    val PrimaryBright = Color(0xFF16B489)
    val Mint = Color(0xFF6EE7B7)
    val Ink = Color(0xFF062A22)
    val InkSoft = Color(0xFF0B3D31)
}

/** Shared gradient brushes so hero areas and primary actions look consistent everywhere. */
object AppGradients {
    val Brand: Brush = Brush.linearGradient(listOf(AppColors.PrimaryBright, AppColors.Primary, AppColors.PrimaryDark))
    val BrandHorizontal: Brush = Brush.horizontalGradient(listOf(AppColors.PrimaryBright, AppColors.Primary))
    val Hero: Brush = Brush.verticalGradient(listOf(AppColors.InkSoft, AppColors.Ink))
    val Warm: Brush = Brush.horizontalGradient(listOf(Color(0xFFFBBF24), AppColors.Accent))
    val Danger: Brush = Brush.horizontalGradient(listOf(Color(0xFFEF4444), AppColors.Error))
    val SoftMint: Brush = Brush.verticalGradient(listOf(AppColors.PrimarySoft, AppColors.Background))
}

// Unified palette aliases so all screens share the single emerald/neutral design system
val IndigoPrimary = AppColors.Primary
val IndigoDark = AppColors.PrimaryDark
val IndigoSoftContainer = AppColors.PrimarySoft
val IndigoOnSoftContainer = AppColors.PrimaryOnSoft

val TealAccent = AppColors.Primary
val TealDark = AppColors.PrimaryDark
val TealSoftContainer = AppColors.PrimarySoft
val TealOnSoftContainer = AppColors.PrimaryOnSoft

val AmberHighlight = AppColors.Accent
val AmberSoftContainer = AppColors.AccentSoft
val AmberOnSoftContainer = AppColors.AccentOnSoft

val BackgroundOffWhite = AppColors.Background
val SurfaceWhite = AppColors.Surface
val SurfaceSubtle = AppColors.SurfaceSubtle
val SurfaceBorder = AppColors.Border

val TextPrimaryCharcoal = AppColors.TextPrimary
val TextSecondaryGrey = AppColors.TextSecondary
val TextMutedGrey = AppColors.TextSecondary

val SuccessGreen = AppColors.VerifiedText
val SuccessContainer = AppColors.VerifiedBg
val WarningAmber = AppColors.AccentOnSoft
val WarningContainer = AppColors.AccentSoft
val ErrorRed = AppColors.Error
val ErrorContainer = AppColors.SosBg
