package com.example.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.chrisbanes.haze.HazeState

/**
 * Floating liquid glass mini-player strip that sits suspended above the navigation bar.
 *
 * Samples backdrop list items tagged with `Modifier.hazeSource(hazeState)`.
 * Displays track metadata, live scrub progress line along the bottom rim, and playback controls.
 */
@Composable
fun GlassMiniPlayer(
    title      : String,
    artist     : String,
    isPlaying  : Boolean,
    progress   : Float,
    hazeState  : HazeState,
    onPlayPause: () -> Unit,
    onNext     : () -> Unit,
    onTap      : () -> Unit,
    modifier   : Modifier  = Modifier,
    artUri     : String?   = null,
    isDarkTheme: Boolean   = false,
) {
    val contentColor = if (isDarkTheme) Color.White else Color.Black

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
            .testTag("glass_mini_player")
    ) {
        GlassSurface(
            hazeState    = hazeState,
            modifier     = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clickable(onClick = onTap),
            cornerRadius = GlassShapes.extraLarge,
            blurRadius   = GlassBlur.thick,
            overlayAlpha = 0.22f,
            isDarkTheme  = isDarkTheme,
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Artwork thumbnail
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!artUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = artUri,
                            contentDescription = "Mini Player Art",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = contentColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Song Title and Artist
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = title,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 14.sp,
                        color      = contentColor,
                    )
                    Text(
                        text     = artist,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                        color    = contentColor.copy(alpha = 0.65f),
                    )
                }

                // Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier.testTag("mini_player_play_pause")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = contentColor,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.testTag("mini_player_next")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Next",
                            tint = contentColor,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            }

            // Live progress line running along bottom rim
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(2.5.dp)
                    .align(Alignment.BottomStart)
                    .background(
                        color = contentColor.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}
