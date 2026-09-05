package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Liquid Glass design system inspired by KMPLiquidGlass & Apple Liquid Glass material.
 * Combines lens refraction, chromatic bevel dispersion, directional specular highlights,
 * and subsurface caustics to produce genuine liquid glass rather than flat transparent overlays.
 */
object GlassTheme {

    // Liquid Glass base material with iridescent refractive depth
    @Composable
    fun liquidGlassSurface(isDark: Boolean = isSystemInDarkTheme()): Brush {
        return if (isDark) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0x4828344D), // refractive crystalline top-left
                    Color(0x28182236), // mid liquid body
                    Color(0x1F0F1726), // deeper refractive bottom
                    Color(0x2E1C273C)  // subtle counter-bounce
                ),
                start = Offset(0f, 0f),
                end = Offset(1000f, 1000f)
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xF5FFFFFF), // luminous crystal top-left
                    Color(0xDDEDF3FC), // fluid center
                    Color(0xCBE2ECFA), // slight prismatic tint
                    Color(0xDDECF5FF)
                ),
                start = Offset(0f, 0f),
                end = Offset(1000f, 1000f)
            )
        }
    }

    // Directional specular gleam (curved lens reflection from top-left)
    @Composable
    fun specularHighlightBrush(isDark: Boolean = isSystemInDarkTheme()): Brush {
        return Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isDark) 0.55f else 0.85f),
                Color(0x60D5EEFF),
                Color.Transparent,
                Color.Transparent
            ),
            start = Offset(0f, 0f),
            end = Offset(400f, 400f)
        )
    }

    // Chromatic aberration border: diamond-cyan highlight on top-left,
    // shifting to rose-violet refraction and subtle ambient occlusion on bottom-right
    @Composable
    fun chromaticBorderBrush(isDark: Boolean = isSystemInDarkTheme()): Brush {
        return if (isDark) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xEEFFFFFF), // direct specular rim
                    Color(0x99B8E2FF), // cyan chromatic refraction
                    Color(0x33A072E8), // violet dispersion
                    Color(0x15080E1A), // shadow absorption
                    Color(0x55E898CE)  // subtle bottom-right caustic bounce
                ),
                start = Offset(0f, 0f),
                end = Offset(1000f, 1000f)
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFFFFF), // pure crystal reflection
                    Color(0xBBD2ECFF), // cyan chromatic refraction
                    Color(0x448B5CF6), // violet prism edge
                    Color(0x1018243C), // shadow edge
                    Color(0x66FFB2D5)  // pink/rose counter reflection
                ),
                start = Offset(0f, 0f),
                end = Offset(1000f, 1000f)
            )
        }
    }

    // Nav Bar Liquid Glass capsule
    @Composable
    fun liquidNavBrush(isDark: Boolean = isSystemInDarkTheme()): Brush {
        return if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0x7520283C),
                    Color(0x55111726),
                    Color(0x660B0F1C)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xF8FFFFFF),
                    Color(0xEEF2F6FD),
                    Color(0xE6E8F0FB)
                )
            )
        }
    }

    // Dynamic background gradient from album artwork palette
    fun dynamicBackgroundGradient(dominantColor: Color, vibrantColor: Color, isDark: Boolean): Brush {
        return if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    dominantColor.copy(alpha = 0.60f),
                    vibrantColor.copy(alpha = 0.35f),
                    Color(0xFF090B12),
                    Color(0xFF07080E)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    dominantColor.copy(alpha = 0.30f),
                    vibrantColor.copy(alpha = 0.18f),
                    Color(0xFFF6F8FD),
                    Color(0xFFF2F5FB)
                )
            )
        }
    }

    // Backward compatible aliases
    @Composable
    fun surfaceBrush(isDark: Boolean): Brush = liquidGlassSurface(isDark)

    @Composable
    fun borderBrush(isDark: Boolean): Brush = chromaticBorderBrush(isDark)

    @Composable
    fun navBarBrush(isDark: Boolean): Brush = liquidNavBrush(isDark)
}
