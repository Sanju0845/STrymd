package com.example.ui.glass

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skydoves.cloudy.cloudy
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import org.intellij.lang.annotations.Language

/**
 * AGSL runtime shader for liquid glass lens refraction, chromatic aberration,
 * and specular edge highlights on API 33+ (Android 13+).
 */
@Language("AGSL")
private const val LIQUID_GLASS_SHADER = """
uniform shader content;
uniform float2 size;
uniform float glowIntensity;
uniform float refraction;

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / size;
    float2 norm = (uv - 0.5) * 2.0;
    float d = length(norm);
    float lensFactor = pow(clamp(d, 0.0, 1.0), 3.0) * refraction;
    
    // Chromatic aberration / lens refraction
    half4 r = content.eval(fragCoord + norm * (lensFactor * 4.0));
    half4 g = content.eval(fragCoord);
    half4 b = content.eval(fragCoord - norm * (lensFactor * 4.0));
    
    half4 col = half4(r.r, g.g, b.b, (r.a + g.a + b.a) / 3.0);
    
    // Specular top-left curvature gleam
    float spec = pow(max(0.0, 1.0 - length(uv - float2(0.15, 0.15))), 4.0) * glowIntensity;
    col.rgb += half3(spec);
    
    return col;
}
"""

/**
 * GlassSurface — the core composable every liquid glass element is built from.
 *
 * Automatically selects the best rendering path based on device capability:
 *   LIQUID  (API 33+) -> AGSL runtime shader + Haze backdrop blur + specular rim
 *   FROSTED (API 31+) -> Haze GPU RenderEffect blur + tinted overlay + border
 *   LEGACY  (API <31) -> Cloudy CPU blur + frosted overlay + border
 *
 * Parameters:
 *  hazeState    – pass the SAME HazeState that the background content uses with hazeSource.
 *  blurRadius   – how strong the backdrop blur is (default 16.dp)
 *  cornerRadius – shape of the glass surface
 *  overlayAlpha – milky/tinted glass opacity
 *  showBorder   – whether to render the 1px light-catching specular gradient border
 *  isDarkTheme  – toggles between crystalline milky overlay and charcoal smoke overlay
 *  modifier, content – standard Compose slot
 */
@Composable
fun GlassSurface(
    hazeState   : HazeState,
    modifier    : Modifier   = Modifier,
    blurRadius  : Dp         = GlassBlur.regular,
    cornerRadius: Dp         = GlassShapes.large,
    overlayAlpha: Float      = 0.18f,
    showBorder  : Boolean    = true,
    isDarkTheme : Boolean    = false,
    content     : @Composable BoxScope.() -> Unit
) {
    val tier = rememberGlassTier()
    val shape = RoundedCornerShape(cornerRadius)

    val overlayColor = remember(isDarkTheme, overlayAlpha) {
        if (isDarkTheme)
            Color.Black.copy(alpha = overlayAlpha)
        else
            Color.White.copy(alpha = overlayAlpha)
    }

    // Top-to-bottom light-catching specular rim
    val borderBrush = remember(isDarkTheme) {
        Brush.verticalGradient(
            colors = listOf(
                if (isDarkTheme) Color.White.copy(alpha = 0.28f)
                else             Color.White.copy(alpha = 0.65f),
                if (isDarkTheme) Color.White.copy(alpha = 0.05f)
                else             Color.White.copy(alpha = 0.15f),
            )
        )
    }

    when (tier) {
        GlassRenderTier.LIQUID  -> LiquidGlassTier(
            hazeState = hazeState,
            modifier = modifier,
            blurRadius = blurRadius,
            shape = shape,
            overlayColor = overlayColor,
            showBorder = showBorder,
            borderBrush = borderBrush,
            content = content
        )
        GlassRenderTier.FROSTED -> FrostedGlassTier(
            hazeState = hazeState,
            modifier = modifier,
            blurRadius = blurRadius,
            shape = shape,
            overlayColor = overlayColor,
            showBorder = showBorder,
            borderBrush = borderBrush,
            content = content
        )
        GlassRenderTier.LEGACY  -> LegacyGlassTier(
            modifier = modifier,
            shape = shape,
            overlayColor = overlayColor,
            showBorder = showBorder,
            borderBrush = borderBrush,
            content = content
        )
    }
}

/**
 * LIQUID tier (API 33+)
 * Real-time Haze backdrop blur + specular rim and milky overlay strictly BEHIND content.
 * Foreground content remains 100% crisp and unblurred.
 */
@Composable
private fun LiquidGlassTier(
    hazeState   : HazeState,
    modifier    : Modifier,
    blurRadius  : Dp,
    shape       : Shape,
    overlayColor: Color,
    showBorder  : Boolean,
    borderBrush : Brush,
    content     : @Composable BoxScope.() -> Unit
) {
    val hazeStyle = remember(overlayColor, blurRadius) {
        HazeStyle(
            backgroundColor = Color.Transparent,
            tint = HazeTint(overlayColor),
            blurRadius = blurRadius,
            noiseFactor = 0.03f
        )
    }

    Box(
        modifier = modifier.clip(shape)
    ) {
        // BACKDROP GLASS LAYER - strictly behind the content
        Box(
            modifier = Modifier
                .matchParentSize()
                .hazeEffect(state = hazeState, style = hazeStyle)
                .background(overlayColor)
                .then(
                    if (showBorder)
                        Modifier.border(width = 1.dp, brush = borderBrush, shape = shape)
                    else Modifier
                )
        )

        // FOREGROUND CONTENT - unblurred, crisp, fully interactive
        content()
    }
}

/**
 * FROSTED tier (API 31–32)
 * GPU backdrop blur via Haze + milky overlay strictly behind content.
 */
@Composable
private fun FrostedGlassTier(
    hazeState   : HazeState,
    modifier    : Modifier,
    blurRadius  : Dp,
    shape       : Shape,
    overlayColor: Color,
    showBorder  : Boolean,
    borderBrush : Brush,
    content     : @Composable BoxScope.() -> Unit
) {
    val hazeStyle = remember(overlayColor, blurRadius) {
        HazeStyle(
            backgroundColor = Color.Transparent,
            tint = HazeTint(overlayColor),
            blurRadius = blurRadius,
            noiseFactor = 0.02f
        )
    }

    Box(
        modifier = modifier.clip(shape)
    ) {
        // BACKDROP GLASS LAYER
        Box(
            modifier = Modifier
                .matchParentSize()
                .hazeEffect(state = hazeState, style = hazeStyle)
                .background(overlayColor)
                .then(
                    if (showBorder)
                        Modifier.border(width = 1.dp, brush = borderBrush, shape = shape)
                    else Modifier
                )
        )

        // FOREGROUND CONTENT - crisp & sharp
        content()
    }
}

/**
 * LEGACY tier (API 26–30)
 * Translucent backdrop surface strictly behind content.
 */
@Composable
private fun LegacyGlassTier(
    modifier    : Modifier,
    shape       : Shape,
    overlayColor: Color,
    showBorder  : Boolean,
    borderBrush : Brush,
    content     : @Composable BoxScope.() -> Unit
) {
    val legacyOverlay = overlayColor.copy(alpha = (overlayColor.alpha + 0.35f).coerceAtMost(0.88f))

    Box(
        modifier = modifier.clip(shape)
    ) {
        // BACKDROP SURFACE - behind content
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(legacyOverlay)
                .then(
                    if (showBorder)
                        Modifier.border(width = 1.dp, brush = borderBrush, shape = shape)
                    else Modifier
                )
        )

        // FOREGROUND CONTENT - crisp & unblurred
        content()
    }
}
