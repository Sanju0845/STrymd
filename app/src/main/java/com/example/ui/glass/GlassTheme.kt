package com.example.ui.glass

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// -----------------------------------------------------------------------------
// GLASS COLOUR TOKENS
// All values in light mode. GlassSurface flips them in dark automatically.
// -----------------------------------------------------------------------------
object GlassColors {
    // The white-tinted overlay sitting on top of the blur — this is the
    // "liquid" look: transparent but with a faint milky white sheen.
    val overlayLight   = Color(0x26FFFFFF)   // ~15% white
    val overlayMedium  = Color(0x40FFFFFF)   // ~25% white
    val overlayDark    = Color(0x0DFFFFFF)   // ~5%  white (subtle)

    // Dark-mode equivalents (charcoal tint instead of white)
    val overlayDarkModeLight  = Color(0x26000000)
    val overlayDarkModeMedium = Color(0x40000000)

    // 1px top/left border highlight that sells the "glass edge" look
    val borderHighlight       = Color(0x33FFFFFF)   // 20% white
    val borderHighlightStrong = Color(0x66FFFFFF)   // 40% white

    // Bottom/right border shadow for depth
    val borderShadow = Color(0x1A000000)   // 10% black

    // Specular rim for LIQUID tier
    val specularRim  = Color(0x80FFFFFF)   // 50% white

    // Fallback tint for LEGACY tier (no real blur)
    val legacyFallback = Color(0xBBFFFFFF) // almost opaque white
}

// -----------------------------------------------------------------------------
// GLASS SHAPE TOKENS
// -----------------------------------------------------------------------------
object GlassShapes {
    val extraSmall = 8.dp
    val small      = 12.dp
    val medium     = 20.dp
    val large      = 28.dp
    val extraLarge = 40.dp
    val pill       = 999.dp  // fully rounded = pill
}

// -----------------------------------------------------------------------------
// GLASS BLUR TOKENS
// -----------------------------------------------------------------------------
object GlassBlur {
    val thin      : Dp = 8.dp    // subtle — top bars, cards
    val regular   : Dp = 16.dp   // standard — nav bar, mini player
    val thick     : Dp = 28.dp   // heavy — bottom sheets, now-playing
    val ultraThick: Dp = 40.dp   // maximum — full-screen overlays
}

// CompositionLocal so any child can query the glass theme without prop-drilling
val LocalGlassTier = staticCompositionLocalOf { GlassRenderTier.FROSTED }
