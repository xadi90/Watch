package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = WatchRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3E121E),
    onPrimaryContainer = Color(0xFFFFD9E0),
    secondary = WatchOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF381F0D),
    onSecondaryContainer = Color(0xFFFFDCC2),
    tertiary = WatchPurple,
    onTertiary = Color.White,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = DividerDark
)

private val LightColorScheme = lightColorScheme(
    primary = WatchRed,
    onPrimary = Color.White,
    secondary = WatchOrange,
    tertiary = WatchPurple,
    background = Color(0xFFF8F9FD),
    onBackground = Color(0xFF13151F),
    surface = Color.White,
    onSurface = Color(0xFF13151F),
    surfaceVariant = Color(0xFFE8EAF2),
    onSurfaceVariant = Color(0xFF4A4E69),
    outline = Color(0xFFD6D9E6)
)

@Composable
fun WatchTheme(
    darkTheme: Boolean = true, // Default to gorgeous dark mode as requested
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
