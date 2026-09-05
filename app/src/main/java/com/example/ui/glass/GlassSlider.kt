package com.example.ui.glass

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Apple Music-style liquid glass scrubber.
 *
 * - Track is thin (2.dp) at rest, expands to 6.dp during scrubbing.
 * - Thumb indicator springs into view while dragging with spring physics.
 * - Haptic feedback on drag start.
 * - [value] range is normalized between 0f and 1f.
 */
@Composable
fun GlassSlider(
    value          : Float,
    onValueChange  : (Float) -> Unit,
    modifier       : Modifier = Modifier,
    trackColor     : Color    = Color.White.copy(alpha = 0.30f),
    progressColor  : Color    = Color.White,
    thumbColor     : Color    = Color.White,
    trackHeight    : Dp       = 2.dp,
    trackHeightDrag: Dp       = 6.dp,
) {
    var isDragging   by remember { mutableStateOf(false) }
    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    val haptic       = LocalHapticFeedback.current
    val density      = LocalDensity.current

    val animTrackH by animateDpAsState(
        targetValue   = if (isDragging) trackHeightDrag else trackHeight,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label         = "trackH"
    )
    val thumbAlpha by animateFloatAsState(
        targetValue   = if (isDragging) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "thumbA"
    )
    val thumbScale by animateFloatAsState(
        targetValue   = if (isDragging) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "thumbS"
    )

    val trackHeightPx = with(density) { animTrackH.toPx() }
    val thumbRadiusPx = with(density) { 8.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .onSizeChanged { trackWidthPx = it.width.toFloat() }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        isDragging = true
                        if (trackWidthPx > 0f) {
                            val new = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                            onValueChange(new)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        try {
                            awaitRelease()
                        } finally {
                            isDragging = false
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        isDragging = true
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onDragEnd   = { isDragging = false },
                    onDragCancel= { isDragging = false },
                    onHorizontalDrag = { change, _ ->
                        if (trackWidthPx > 0f) {
                            val new = (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                            onValueChange(new)
                        }
                    }
                )
            }
            .drawBehind {
                val cy = size.height / 2f
                val clampedValue = value.coerceIn(0f, 1f)

                // Background track
                drawLine(
                    color       = trackColor,
                    start       = Offset(0f, cy),
                    end         = Offset(size.width, cy),
                    strokeWidth = trackHeightPx,
                    cap         = StrokeCap.Round,
                )

                // Progress active fill
                drawLine(
                    color       = progressColor,
                    start       = Offset(0f, cy),
                    end         = Offset(size.width * clampedValue, cy),
                    strokeWidth = trackHeightPx,
                    cap         = StrokeCap.Round,
                )

                // Spring-animated thumb
                val thumbX = size.width * clampedValue
                if (thumbScale > 0.01f) {
                    drawCircle(
                        color  = thumbColor.copy(alpha = thumbAlpha),
                        radius = thumbRadiusPx * thumbScale,
                        center = Offset(thumbX, cy),
                    )
                }
            }
    )
}
