package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppThemeMode {
    DARK,
    LIGHT,
    CYBERPUNK,
    AMOLED
}

private val ModernDarkColorScheme = darkColorScheme(
    primary = SleekBlue,
    onPrimary = Color.White,
    primaryContainer = SleekBlueVibrant,
    onPrimaryContainer = Color.White,
    secondary = SleekPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF581C87),
    onSecondaryContainer = Color(0xFFF3E8FF),
    tertiary = SleekYellow,
    onTertiary = Color.Black,
    background = SleekBackground,
    onBackground = SleekTextPrimary,
    surface = SleekSurface,
    onSurface = SleekTextPrimary,
    surfaceVariant = SleekSurfaceVariant,
    onSurfaceVariant = SleekTextSecondary,
    outline = SleekBorder
)

private val ModernLightColorScheme = lightColorScheme(
    primary = Cyan40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBAE6FD),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF7C3AED),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDD6FE),
    onSecondaryContainer = Color(0xFF4C1D95),
    tertiary = AmberAccent,
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

private val CyberpunkColorScheme = darkColorScheme(
    primary = CyberpunkPrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003847),
    onPrimaryContainer = CyberpunkPrimary,
    secondary = CyberpunkSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF470020),
    onSecondaryContainer = Color(0xFFFF80BF),
    tertiary = CyberpunkAccent,
    onTertiary = Color.Black,
    background = CyberpunkBackground,
    onBackground = Color(0xFFE2E8F0),
    surface = CyberpunkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E1738),
    onSurfaceVariant = Color(0xFFD8B4FE),
    outline = Color(0xFF6B21A8)
)

private val AmoledColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF164E63),
    onPrimaryContainer = Color(0xFFCFFAFE),
    secondary = EmeraldNeon,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = AmberAccent,
    onTertiary = Color.Black,
    background = AmoledBackground,
    onBackground = Color(0xFFFFFFFF),
    surface = AmoledSurface,
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = AmoledCard,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF262626)
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme: ColorScheme = when (themeMode) {
        AppThemeMode.LIGHT -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicLightColorScheme(context)
            } else {
                ModernLightColorScheme
            }
        }
        AppThemeMode.CYBERPUNK -> CyberpunkColorScheme
        AppThemeMode.AMOLED -> AmoledColorScheme
        AppThemeMode.DARK -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicDarkColorScheme(context)
            } else {
                ModernDarkColorScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
