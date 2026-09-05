package com.example.ui.nowplaying

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.AuraDarkBackground

/**
 * Immersive blurred album artwork and atmospheric ambient light blooms
 * placed strictly BEHIND the music player screen.
 */
@Composable
fun NowPlayingBlurredBackdrop(
    artUri       : String?,
    dominantColor: Color,
    vibrantColor : Color,
    isPlaying    : Boolean,
    modifier     : Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambientPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue  = if (isPlaying) 0.82f else 0.55f,
        animationSpec= infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue  = if (isPlaying) 1.12f else 1.0f,
        animationSpec= infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuraDarkBackground)
    ) {
        // 1. Zoomed and heavily blurred album artwork layer
        if (!artUri.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            renderEffect = RenderEffect
                                .createBlurEffect(110f, 110f, Shader.TileMode.CLAMP)
                                .asComposeRenderEffect()
                        }
                    }
                    .then(
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                            Modifier.blur(radius = 40.dp)
                        } else Modifier
                    )
            ) {
                AsyncImage(
                    model              = artUri,
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .fillMaxSize()
                        .scale(1.35f)
                        .alpha(0.70f)
                )
            }
        }

        // 2. Atmospheric vibrant color light blooms (Upper and Lower)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-60).dp, y = (-40).dp)
                .size(360.dp)
                .scale(pulseScale)
                .alpha(pulseAlpha)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            dominantColor.copy(alpha = 0.85f),
                            dominantColor.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
                .blur(radius = 60.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 80.dp, y = 60.dp)
                .size(400.dp)
                .scale(pulseScale * 1.05f)
                .alpha(pulseAlpha * 0.90f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            vibrantColor.copy(alpha = 0.75f),
                            vibrantColor.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
                .blur(radius = 70.dp)
        )

        // 3. Dark gradient scrim overlay to guarantee pristine contrast and legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.75f),
                            Color.Black.copy(alpha = 0.92f)
                        )
                    )
                )
        )
    }
}
