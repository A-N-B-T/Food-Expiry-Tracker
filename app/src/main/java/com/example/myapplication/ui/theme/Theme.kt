package com.example.myapplication.ui.theme

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
    primary = Blue80,
    onPrimary = Color(0xFF003355),
    primaryContainer = Color(0xFF11456A),
    onPrimaryContainer = Color(0xFFD3E4FF),
    secondary = Blue80,
    onSecondary = Color(0xFF003355),
    secondaryContainer = Color(0xFF11456A),
    onSecondaryContainer = Color(0xFFD3E4FF),
    tertiary = Blue80,
    onTertiary = Color(0xFF003355),
    tertiaryContainer = Color(0xFF11456A),
    onTertiaryContainer = Color(0xFFD3E4FF),

    background = Color(0xFF090B0D),
    onBackground = Color(0xFFE7E9ED),
    surface = Color(0xFF101214),
    onSurface = Color(0xFFE7E9ED),
    surfaceVariant = Color(0xFF1A1D22),
    onSurfaceVariant = Color(0xFFC2C7D0),
    outline = Color(0xFF8A9099),
    outlineVariant = Color(0xFF3A4048),
    surfaceBright = Color(0xFF2A2E32),
    surfaceDim = Color(0xFF090B0D),
    surfaceContainerLowest = Color(0xFF050608),
    surfaceContainerLow = Color(0xFF0E1012),
    surfaceContainer = Color(0xFF131619),
    surfaceContainerHigh = Color(0xFF1A1D20),
    surfaceContainerHighest = Color(0xFF212529)
)

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Blue40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3F2FD),
    onSecondaryContainer = Color(0xFF0D47A1),
    tertiary = Blue40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE3F2FD),
    onTertiaryContainer = Color(0xFF0D47A1),

    background = Color.White,
    onBackground = Color(0xFF191C20),
    surface = Color.White,
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFF5F7FA),
    onSurfaceVariant = Color(0xFF5F6368),
    outline = Color(0xFFB8C0CC),
    outlineVariant = Color(0xFFE0E5EC),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFBFCFD),
    surfaceContainer = Color(0xFFF7F9FC),
    surfaceContainerHigh = Color(0xFFF1F4F8),
    surfaceContainerHighest = Color(0xFFEBEFF4)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
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
