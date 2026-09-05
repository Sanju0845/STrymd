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
import com.example.data.preferences.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = AuraAccentRed,
    onPrimary = Color.White,
    primaryContainer = AuraAccentPink.copy(alpha = 0.25f),
    onPrimaryContainer = AuraAccentPink,
    secondary = AuraAccentViolet,
    onSecondary = Color.White,
    tertiary = AuraAccentBlue,
    background = AuraDarkBackground,
    onBackground = Color(0xFFF1F3F7),
    surface = AuraDarkSurface,
    onSurface = Color(0xFFF1F3F7),
    surfaceVariant = AuraDarkCard,
    onSurfaceVariant = Color(0xFF9EA6B8),
    outline = Color(0xFF323A4E)
)

private val LightColorScheme = lightColorScheme(
    primary = AuraAccentRed,
    onPrimary = Color.White,
    primaryContainer = AuraAccentPink.copy(alpha = 0.15f),
    onPrimaryContainer = AuraAccentRed,
    secondary = AuraAccentViolet,
    onSecondary = Color.White,
    tertiary = AuraAccentBlue,
    background = AuraLightBackground,
    onBackground = Color(0xFF131722),
    surface = AuraLightSurface,
    onSurface = Color(0xFF131722),
    surfaceVariant = AuraLightCard,
    onSurfaceVariant = Color(0xFF555E70),
    outline = Color(0xFFD4DAE6)
)

@Composable
fun AuraTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false, // prioritize Aura's signature liquid glass branding
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

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
        typography = AuraTypography,
        shapes = AuraShapes,
        content = content
    )
}
