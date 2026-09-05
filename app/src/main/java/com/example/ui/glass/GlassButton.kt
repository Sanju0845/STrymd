package com.example.ui.glass

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState

/**
 * Pill-shaped liquid glass button with spring tactile feedback.
 */
@Composable
fun GlassPillButton(
    text        : String,
    onClick     : () -> Unit,
    hazeState   : HazeState,
    modifier    : Modifier     = Modifier,
    icon        : ImageVector? = null,
    isDarkTheme : Boolean      = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "btnScale"
    )

    GlassSurface(
        hazeState    = hazeState,
        modifier     = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(GlassShapes.pill))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        cornerRadius = GlassShapes.pill,
        blurRadius   = GlassBlur.regular,
        overlayAlpha = 0.22f,
        isDarkTheme  = isDarkTheme,
    ) {
        Row(
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement= Arrangement.Center,
            modifier             = Modifier.padding(horizontal = 4.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = if (isDarkTheme) Color.White else Color.Black,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text       = text,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 15.sp,
                color      = if (isDarkTheme) Color.White else Color.Black,
            )
        }
    }
}

/**
 * Circular liquid glass icon button with spring scale compression on press.
 */
@Composable
fun GlassIconButton(
    icon        : ImageVector,
    contentDesc : String,
    onClick     : () -> Unit,
    hazeState   : HazeState,
    modifier    : Modifier  = Modifier,
    size        : Dp        = 44.dp,
    isDarkTheme : Boolean   = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "iconBtnScale"
    )

    GlassSurface(
        hazeState    = hazeState,
        modifier     = modifier
            .size(size)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(GlassShapes.pill))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        cornerRadius = GlassShapes.pill,
        blurRadius   = GlassBlur.thin,
        overlayAlpha = 0.18f,
        isDarkTheme  = isDarkTheme,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector        = icon,
                contentDescription = contentDesc,
                tint               = if (isDarkTheme) Color.White else Color.Black,
                modifier           = Modifier.size(size * 0.45f),
            )
        }
    }
}
