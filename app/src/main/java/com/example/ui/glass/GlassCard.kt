package com.example.ui.glass

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState

/**
 * A content card with liquid glass backdrop blur, specular rim, and inner padding.
 */
@Composable
fun GlassCard(
    hazeState   : HazeState,
    modifier    : Modifier  = Modifier,
    cornerRadius: Dp        = GlassShapes.large,
    blurRadius  : Dp        = GlassBlur.thin,
    overlayAlpha: Float     = 0.16f,
    isDarkTheme : Boolean   = false,
    content     : @Composable ColumnScope.() -> Unit
) {
    GlassSurface(
        hazeState    = hazeState,
        modifier     = modifier,
        blurRadius   = blurRadius,
        cornerRadius = cornerRadius,
        overlayAlpha = overlayAlpha,
        isDarkTheme  = isDarkTheme,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content  = content
        )
    }
}
