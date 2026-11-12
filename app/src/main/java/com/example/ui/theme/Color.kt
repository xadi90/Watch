package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Premium Dark Theme Palette inspired by high-end multimedia apps
val BackgroundDark = Color(0xFF090A0F)
val SurfaceDark = Color(0xFF13151F)
val SurfaceVariantDark = Color(0xFF1C1F2E)
val SurfaceCard = Color(0xFF181B28)

// Dynamic Neon & Gradient Accents (Crimson Red, Sunset Orange, Hot Pink, Violet Purple)
val WatchRed = Color(0xFFFF2A54)
val WatchOrange = Color(0xFFFF7A00)
val WatchPink = Color(0xFFFF007A)
val WatchPurple = Color(0xFF9D00FF)
val WatchCyan = Color(0xFF00E5FF)
val WatchGreen = Color(0xFF00E676)

val TextPrimaryDark = Color(0xFFF6F7FB)
val TextSecondaryDark = Color(0xFFA0A5BD)
val TextTertiaryDark = Color(0xFF6B728E)

val DividerDark = Color(0xFF23273A)

// Gradients
val WatchFlameGradient = Brush.horizontalGradient(
    colors = listOf(WatchRed, WatchOrange)
)

val WatchNeonGradient = Brush.horizontalGradient(
    colors = listOf(WatchPink, WatchPurple)
)

val WatchHeroGradient = Brush.linearGradient(
    colors = listOf(WatchRed, WatchPink, WatchPurple)
)

val CardGlowGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF2B1F3D), SurfaceCard)
)
