package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassCardShape
import com.example.ui.theme.GlassTheme

/**
 * Genuine Liquid Glass Surface component based on KMPLiquidGlass principles.
 * Rather than a flat semi-transparent overlay, it simulates an optical glass lens:
 * - Refractive iridescent base layer
 * - Curvature specular gloss highlight arc
 * - Chromatic aberration rim (diamond-cyan top-left to violet-rose bottom-right)
 * - Inner bevel depth highlight
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = GlassCardShape,
    elevation: Dp = 10.dp,
    borderWidth: Dp = 1.2.dp,
    customSurfaceBrush: Brush? = null,
    enableSpecularHighlight: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val surfaceBrush = customSurfaceBrush ?: GlassTheme.liquidGlassSurface(isDark)
    val borderBrush = GlassTheme.chromaticBorderBrush(isDark)

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = if (isDark) Color(0x70000000) else Color(0x35142238),
                spotColor = if (isDark) Color(0x90030509) else Color(0x25142238)
            )
            .clip(shape)
            .background(surfaceBrush)
            .drawWithContent {
                drawContent()

                // Specular lens curvature sheen across top-left quadrant
                if (enableSpecularHighlight) {
                    val specularBrush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.22f else 0.45f),
                            Color(0x35C2E8FF),
                            Color.Transparent
                        ),
                        center = Offset(0f, 0f),
                        radius = size.minDimension * 0.9f
                    )
                    drawRect(brush = specularBrush)

                    // Subtle inner caustic bevel edge at top
                    val innerBevelBrush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.35f else 0.60f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = 16.dp.toPx()
                    )
                    drawRect(brush = innerBevelBrush)
                }
            }
            .border(width = borderWidth, brush = borderBrush, shape = shape),
        content = content
    )
}
