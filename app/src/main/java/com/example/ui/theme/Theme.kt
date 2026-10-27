package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MedicalTealPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = MedicalTealDark,
    onPrimaryContainer = Color.White,
    secondary = MedicalCyanSecondary,
    onSecondary = Color.Black,
    tertiary = MedicalMint,
    background = MedicalSurfaceDark,
    onBackground = Color(0xFFE2E8F0),
    surface = MedicalCardBgDark,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = MedicalBorderDark,
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = MedicalAlertRed
)

private val LightColorScheme = lightColorScheme(
    primary = MedicalTealPrimary,
    onPrimary = Color.White,
    primaryContainer = MedicalSurfaceVariantLight,
    onPrimaryContainer = MedicalNavyHeader,
    secondary = MedicalMint,
    onSecondary = Color.White,
    tertiary = MedicalNavyHeader,
    background = MedicalSurfaceLight,
    onBackground = Color(0xFF0F172A),
    surface = MedicalCardBg,
    onSurface = Color(0xFF1E293B),
    surfaceVariant = MedicalSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF475569),
    error = MedicalAlertRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent medical branding
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
