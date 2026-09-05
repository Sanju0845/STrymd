package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.R
import com.example.ui.theme.AlbumArtShape
import com.example.ui.theme.AuraAccentPink
import com.example.ui.theme.AuraAccentRed
import com.example.ui.theme.AuraAccentViolet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AlbumArtImage(
    artUri: String?,
    modifier: Modifier = Modifier,
    shape: Shape = AlbumArtShape,
    contentDescription: String? = "Album Artwork",
    contentScale: ContentScale = ContentScale.Crop
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        AuraAccentRed.copy(alpha = 0.5f),
                        AuraAccentViolet.copy(alpha = 0.6f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!artUri.isNullOrEmpty()) {
            AsyncImage(
                model = artUri,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.strymd_logo),
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(0.7f)
            )
        }
    }
}

@Composable
fun rememberDynamicPalette(
    artUri: String?,
    isDark: Boolean = isSystemInDarkTheme()
): Pair<Color, Color> {
    val context = LocalContext.current
    val defaultDominant = if (isDark) AuraAccentRed.copy(alpha = 0.7f) else AuraAccentRed.copy(alpha = 0.4f)
    val defaultVibrant = if (isDark) AuraAccentViolet.copy(alpha = 0.5f) else AuraAccentPink.copy(alpha = 0.3f)

    var colors by remember(artUri) {
        mutableStateOf(Pair(defaultDominant, defaultVibrant))
    }

    LaunchedEffect(artUri) {
        if (artUri.isNullOrEmpty()) {
            colors = Pair(defaultDominant, defaultVibrant)
            return@LaunchedEffect
        }

        withContext(Dispatchers.IO) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(artUri)
                    .allowHardware(false)
                    .size(200, 200)
                    .build()

                val result = context.imageLoader.execute(request)
                if (result is SuccessResult) {
                    val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                    if (bitmap != null) {
                        val palette = Palette.from(bitmap).generate()
                        val dominant = palette.getDominantColor(android.graphics.Color.parseColor("#FA2356"))
                        val vibrant = palette.getVibrantColor(
                            palette.getMutedColor(android.graphics.Color.parseColor("#8E52F6"))
                        )
                        withContext(Dispatchers.Main) {
                            colors = Pair(Color(dominant), Color(vibrant))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    return colors
}
