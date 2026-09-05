package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AuraAccentRed
import java.util.Locale

@Composable
fun SmoothScrubber(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.White.copy(alpha = 0.20f),
    showTimeLabels: Boolean = true
) {
    val view = LocalView.current
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val currentFraction = if (isDragging) {
        dragFraction
    } else {
        if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    }

    val trackHeight by animateDpAsState(
        targetValue = if (isDragging) 8.dp else 4.dp,
        animationSpec = spring(),
        label = "trackHeight"
    )

    val thumbAlpha by animateFloatAsState(
        targetValue = if (isDragging) 1f else 0f,
        animationSpec = spring(),
        label = "thumbAlpha"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onSeek((fraction * durationMs).toLong())
                    }
                }
                .pointerInput(durationMs) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragFraction = (offset.x / size.width).coerceIn(0f, 1f)
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        },
                        onDragEnd = {
                            isDragging = false
                            onSeek((dragFraction * durationMs).toLong())
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val newFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                            if ((newFraction * 50).toInt() != (dragFraction * 50).toInt()) {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            }
                            dragFraction = newFraction
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val h = trackHeight.toPx()
                val topY = (canvasHeight - h) / 2f
                val cornerRadius = CornerRadius(h / 2f, h / 2f)

                // Background inactive track
                drawRoundRect(
                    color = inactiveColor,
                    topLeft = Offset(0f, topY),
                    size = Size(canvasWidth, h),
                    cornerRadius = cornerRadius
                )

                // Active filled track
                val activeWidth = canvasWidth * currentFraction
                if (activeWidth > 0) {
                    drawRoundRect(
                        color = activeColor,
                        topLeft = Offset(0f, topY),
                        size = Size(activeWidth, h),
                        cornerRadius = cornerRadius
                    )
                }

                // Thumb circle (visible while dragging)
                if (thumbAlpha > 0f) {
                    val thumbRadius = (h * 1.5f) / 2f
                    drawCircle(
                        color = Color.White.copy(alpha = thumbAlpha),
                        radius = thumbRadius,
                        center = Offset(activeWidth.coerceIn(thumbRadius, canvasWidth - thumbRadius), canvasHeight / 2f)
                    )
                }
            }
        }

        if (showTimeLabels) {
            val displayPos = if (isDragging) (dragFraction * durationMs).toLong() else positionMs
            val remainingMs = (durationMs - displayPos).coerceAtLeast(0L)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(displayPos),
                    style = MaterialTheme.typography.labelSmall,
                    color = activeColor.copy(alpha = 0.75f)
                )
                Text(
                    text = "-${formatDuration(remainingMs)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = activeColor.copy(alpha = 0.75f)
                )
            }
        }
    }
}

fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
