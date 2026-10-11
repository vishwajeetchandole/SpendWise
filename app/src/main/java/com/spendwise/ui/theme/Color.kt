package com.spendwise.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// SpendWise Pure Blue & White Brand Colors (No green gradient)
val BrandRoyalBlue = Color(0xFF1D4ED8)
val BrandPrimaryBlue = Color(0xFF2563EB)
val BrandSkyBlue = Color(0xFF3B82F6)
val BrandDeepNavy = Color(0xFF0F294A)
val BrandDarkNavy = Color(0xFF0A192F)
val BrandUltraDark = Color(0xFF06101E)

// Primary Gradient Brushes (Pure Blue Spectrum - Royal Blue to Sapphire Blue)
val SpendWiseBrandGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF1E40AF), Color(0xFF2563EB), Color(0xFF3B82F6))
)

val SpendWiseHeaderGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF1E3A8A), Color(0xFF1D4ED8), Color(0xFF2563EB))
)

val SpendWiseHeaderCardGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF1E3A8A), Color(0xFF1D4ED8), Color(0xFF3B82F6))
)

val SpendWiseButtonGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF1D4ED8), Color(0xFF2563EB), Color(0xFF3B82F6))
)

// Material 3 Theme Palette - Light Scheme (Crisp White & Royal Blue)
val PrimaryLight = Color(0xFF1D4ED8)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFDBEAFE)
val OnPrimaryContainerLight = Color(0xFF1E3A8A)

val SecondaryLight = Color(0xFF2563EB)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFE0E7FF)
val OnSecondaryContainerLight = Color(0xFF1E3A8A)

val TertiaryLight = Color(0xFF0284C7)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFE0F2FE)
val OnTertiaryContainerLight = Color(0xFF0C4A6E)

val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFEFF4FB)
val OnBackgroundLight = Color(0xFF0F172A)
val OnSurfaceLight = Color(0xFF0F172A)
val OnSurfaceVariantLight = Color(0xFF475569)
val OutlineLight = Color(0xFFCBD5E1)

// Material 3 Theme Palette - Dark Scheme (Deep Navy & Brilliant Sky Blue)
val PrimaryDark = Color(0xFF60A5FA)
val OnPrimaryDark = Color(0xFF082046)
val PrimaryContainerDark = Color(0xFF1E3A8A)
val OnPrimaryContainerDark = Color(0xFFDBEAFE)

val SecondaryDark = Color(0xFF93C5FD)
val OnSecondaryDark = Color(0xFF0C2D61)
val SecondaryContainerDark = Color(0xFF1E40AF)
val OnSecondaryContainerDark = Color(0xFFDBEAFE)

val TertiaryDark = Color(0xFF38BDF8)
val OnTertiaryDark = Color(0xFF082F49)
val TertiaryContainerDark = Color(0xFF0369A1)
val OnTertiaryContainerDark = Color(0xFFE0F2FE)

val BackgroundDark = Color(0xFF071120)
val SurfaceDark = Color(0xFF0F1D33)
val SurfaceVariantDark = Color(0xFF172A46)
val OnBackgroundDark = Color(0xFFF1F5F9)
val OnSurfaceDark = Color(0xFFF1F5F9)
val OnSurfaceVariantDark = Color(0xFF94A3B8)
val OutlineDark = Color(0xFF294166)

// Status & Indicator Colors
val ExpenseRed = Color(0xFFEF4444)
val WarningAmber = Color(0xFFF59E0B)
val SuccessGreen = Color(0xFF2563EB)
val InfoBlue = Color(0xFF2563EB)

fun parseHexColor(hex: String, fallback: Color = Color(0xFF2563EB)): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }
}
