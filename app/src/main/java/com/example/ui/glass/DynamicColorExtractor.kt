package com.example.ui.glass

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Given an album art [bitmap], extracts the vibrant or dominant color using AndroidX Palette
 * and returns it as an animated Compose [State] of [Color] so colors crossfade smoothly on track change.
 */
@Composable
fun rememberDynamicColor(
    bitmap      : Bitmap?,
    darkFallback: Color = Color(0xFF1C1C1E),
): State<Color> {
    var extracted by remember { mutableStateOf(darkFallback) }

    LaunchedEffect(bitmap) {
        if (bitmap == null) {
            extracted = darkFallback
            return@LaunchedEffect
        }
        withContext(Dispatchers.Default) {
            val palette = Palette.from(bitmap).generate()
            val argb = palette.getVibrantColor(
                palette.getMutedColor(
                    palette.getDominantColor(darkFallback.toArgb())
                )
            )
            extracted = Color(argb)
        }
    }

    return animateColorAsState(
        targetValue   = extracted,
        animationSpec = tween(durationMillis = 700),
        label         = "dynamicColor",
    )
}

/**
 * Convenience overload that takes an [artUri] string and loads it asynchronously via Coil,
 * then extracts the vibrant palette color with a smooth crossfade.
 */
@Composable
fun rememberDynamicColorFromUri(
    artUri      : String?,
    fallback    : Color = Color(0xFF1E222D),
): State<Color> {
    val context = LocalContext.current
    var extracted by remember { mutableStateOf(fallback) }

    LaunchedEffect(artUri) {
        if (artUri.isNullOrEmpty()) {
            extracted = fallback
            return@LaunchedEffect
        }

        withContext(Dispatchers.IO) {
            try {
                val imageLoader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(artUri)
                    .allowHardware(false)
                    .size(200, 200)
                    .build()
                val result = imageLoader.execute(request)
                if (result is SuccessResult) {
                    val bitmap = result.drawable.toBitmap()
                    val palette = Palette.from(bitmap).generate()
                    val argb = palette.getVibrantColor(
                        palette.getMutedColor(
                            palette.getDominantColor(fallback.toArgb())
                        )
                    )
                    extracted = Color(argb)
                }
            } catch (e: Exception) {
                extracted = fallback
            }
        }
    }

    return animateColorAsState(
        targetValue   = extracted,
        animationSpec = tween(durationMillis = 700),
        label         = "dynamicColorFromUri",
    )
}
