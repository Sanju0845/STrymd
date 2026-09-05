package com.example.ui.nowplaying

import android.content.Context
import android.media.AudioManager
import com.example.ui.glass.GlassSlider
import com.example.ui.glass.rememberDynamicColorFromUri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.VolumeDown
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.example.domain.model.Song
import com.example.playback.EqualizerController
import com.example.playback.PlaybackManager
import com.example.playback.PlaybackUiState
import com.example.playback.RepeatMode
import com.example.ui.components.AlbumArtImage
import com.example.ui.components.GlassSurface
import com.example.ui.components.SmoothScrubber
import com.example.ui.components.rememberDynamicPalette
import com.example.ui.theme.AuraAccentPink
import com.example.ui.theme.AuraAccentRed
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.GlassTheme
import com.example.ui.theme.PillShape
import kotlin.math.roundToInt

enum class NowPlayingTab {
    PLAYER, LYRICS, QUEUE
}

@Composable
fun NowPlayingScreen(
    playbackManager: PlaybackManager,
    playbackState: PlaybackUiState,
    onToggleFavorite: (Song) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val song = playbackState.currentSong ?: return
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    var currentTab by remember { mutableStateOf(NowPlayingTab.PLAYER) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showEqualizerDialog by remember { mutableStateOf(false) }

    // Dynamic Palette Color Extraction from Album Art
    val (dominantColor, vibrantColor) = rememberDynamicPalette(song.albumArtUri, isDark = true)
    val animatedDominant by animateColorAsState(targetValue = dominantColor, animationSpec = spring(), label = "dominantColor")
    val animatedVibrant by animateColorAsState(targetValue = vibrantColor, animationSpec = spring(), label = "vibrantColor")

    // Dynamic Art scaling on play/pause (0.91x paused -> 1.0x playing)
    val artScale by animateFloatAsState(
        targetValue = if (playbackState.isPlaying) 1.0f else 0.91f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "artScale"
    )

    // Interactive swipe-down and horizontal swipe gestures
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var horizontalDragOffset by remember { mutableFloatStateOf(0f) }
    val animatedOffsetY by animateFloatAsState(
        targetValue = dragOffsetY,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "dragOffsetY"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(0, animatedOffsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        dragOffsetY = 0f
                        horizontalDragOffset = 0f
                    },
                    onDragEnd = {
                        if (dragOffsetY > 120f) {
                            if (currentTab != NowPlayingTab.PLAYER) {
                                currentTab = NowPlayingTab.PLAYER
                                dragOffsetY = 0f
                            } else {
                                onDismiss()
                            }
                        } else if (horizontalDragOffset < -60f) {
                            playbackManager.next()
                        } else if (horizontalDragOffset > 60f) {
                            playbackManager.previous()
                        }
                        dragOffsetY = 0f
                        horizontalDragOffset = 0f
                    },
                    onDragCancel = {
                        dragOffsetY = 0f
                        horizontalDragOffset = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (kotlin.math.abs(dragAmount.y) > kotlin.math.abs(dragAmount.x) || dragOffsetY > 0) {
                            if (dragAmount.y > 0 || dragOffsetY > 0) {
                                dragOffsetY = (dragOffsetY + dragAmount.y).coerceAtLeast(0f)
                            }
                        } else {
                            horizontalDragOffset += dragAmount.x
                        }
                    }
                )
            }
            .testTag("now_playing_screen")
    ) {
        // Authentic Liquid Blurred Artwork & Atmospheric Ambient Glow behind the Music Player Screen
        NowPlayingBlurredBackdrop(
            artUri        = song.albumArtUri,
            dominantColor = animatedDominant,
            vibrantColor  = animatedVibrant,
            isPlaying     = playbackState.isPlaying
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Grab Bar & Dismiss / Tab Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentTab != NowPlayingTab.PLAYER) {
                            currentTab = NowPlayingTab.PLAYER
                        } else {
                            onDismiss()
                        }
                    },
                    modifier = Modifier.testTag("now_playing_dismiss_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Apple Music pull pill indicator
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.35f))
                )

                // Sleep timer indicator or toggle button
                IconButton(onClick = { showSleepTimerDialog = true }) {
                    Icon(
                        imageVector = Icons.Rounded.Bedtime,
                        contentDescription = "Sleep Timer",
                        tint = if (playbackState.sleepTimerMinutesRemaining != null) AuraAccentRed else Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Central View: Player, Lyrics, or Queue
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentTab) {
                    NowPlayingTab.PLAYER -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Centered Large Album Artwork with drop shadow
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.88f)
                                    .aspectRatio(1f)
                                    .scale(artScale)
                                    .shadow(
                                        elevation = 32.dp,
                                        shape = RoundedCornerShape(24.dp),
                                        spotColor = animatedDominant.copy(alpha = 0.6f)
                                    )
                                    .clip(RoundedCornerShape(24.dp))
                                    .testTag("now_playing_album_art")
                            ) {
                                AlbumArtImage(
                                    artUri = song.albumArtUri,
                                    modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(24.dp)
                                )
                            }

                            // Song Title, Artist and Favorite Button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = song.artist,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = { onToggleFavorite(song) },
                                    modifier = Modifier.testTag("now_playing_favorite_button")
                                ) {
                                    Icon(
                                        imageVector = if (song.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (song.isFavorite) AuraAccentRed else Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            // Apple Music-style dynamic glass scrubber
                            Column(modifier = Modifier.fillMaxWidth()) {
                                val currentProgress = if (playbackState.durationMs > 0) {
                                    (playbackState.currentPositionMs.toFloat() / playbackState.durationMs).coerceIn(0f, 1f)
                                } else 0f

                                GlassSlider(
                                    value = currentProgress,
                                    onValueChange = { frac ->
                                        playbackManager.seekTo((frac * playbackState.durationMs).toLong())
                                    },
                                    trackColor = Color.White.copy(alpha = 0.25f),
                                    progressColor = Color.White,
                                    thumbColor = Color.White,
                                    modifier = Modifier.testTag("now_playing_scrubber")
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = formatTime(playbackState.currentPositionMs),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.65f)
                                    )
                                    Text(
                                        text = formatTime(playbackState.durationMs),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.65f)
                                    )
                                }
                            }

                            // Main Playback Controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Shuffle
                                IconButton(
                                    onClick = { playbackManager.toggleShuffle() },
                                    modifier = Modifier.testTag("now_playing_shuffle")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Shuffle,
                                        contentDescription = "Shuffle",
                                        tint = if (playbackState.isShuffleEnabled) AuraAccentRed else Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                // Previous
                                IconButton(
                                    onClick = { playbackManager.previous() },
                                    modifier = Modifier.testTag("now_playing_previous")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SkipPrevious,
                                        contentDescription = "Previous",
                                        tint = Color.White,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }

                                // Play / Pause Big Morphing Button with bouncy spring
                                val playPauseScale by animateFloatAsState(
                                    targetValue = if (playbackState.isPlaying) 1.0f else 0.94f,
                                    animationSpec = spring(dampingRatio = 0.5f),
                                    label = "playScale"
                                )

                                GlassSurface(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .scale(playPauseScale)
                                        .clip(CircleShape)
                                        .testTag("now_playing_play_pause"),
                                    shape = CircleShape,
                                    elevation = 8.dp
                                ) {
                                    IconButton(
                                        onClick = { playbackManager.togglePlayPause() },
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            imageVector = if (playbackState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                            contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                                            tint = Color.White,
                                            modifier = Modifier.size(42.dp)
                                        )
                                    }
                                }

                                // Next
                                IconButton(
                                    onClick = { playbackManager.next() },
                                    modifier = Modifier.testTag("now_playing_next")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.SkipNext,
                                        contentDescription = "Next",
                                        tint = Color.White,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }

                                // Repeat
                                IconButton(
                                    onClick = { playbackManager.toggleRepeat() },
                                    modifier = Modifier.testTag("now_playing_repeat")
                                ) {
                                    val repeatIcon = when (playbackState.repeatMode) {
                                        RepeatMode.ONE -> Icons.Rounded.RepeatOne
                                        else -> Icons.Rounded.Repeat
                                    }
                                    val repeatTint = when (playbackState.repeatMode) {
                                        RepeatMode.OFF -> Color.White.copy(alpha = 0.5f)
                                        else -> AuraAccentRed
                                    }
                                    Icon(
                                        imageVector = repeatIcon,
                                        contentDescription = "Repeat",
                                        tint = repeatTint,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            // Volume Bar (Apple-style)
                            var volumeFraction by remember {
                                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
                                val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
                                mutableFloatStateOf(if (maxVol > 0) currentVol / maxVol else 0.5f)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.VolumeDown,
                                    contentDescription = "Volume Down",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Slider(
                                    value = volumeFraction,
                                    onValueChange = { frac ->
                                        volumeFraction = frac
                                        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                        val targetVol = (frac * maxVol).toInt()
                                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color.White.copy(alpha = 0.8f),
                                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Rounded.VolumeUp,
                                    contentDescription = "Volume Up",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    NowPlayingTab.LYRICS -> {
                        LyricsView(
                            lyricsText = song.lyrics,
                            currentPositionMs = playbackState.currentPositionMs,
                            onSeek = { pos -> playbackManager.seekTo(pos) }
                        )
                    }

                    NowPlayingTab.QUEUE -> {
                        QueueView(
                            queue = playbackState.queue,
                            currentIndex = playbackState.queueIndex,
                            onSongSelected = { targetSong -> playbackManager.playSong(targetSong, playbackState.queue) },
                            onRemoveFromQueue = { index -> playbackManager.removeFromQueue(index) },
                            onReorderQueue = { from, to -> playbackManager.reorderQueue(from, to) }
                        )
                    }
                }
            }

            // Bottom Bar (Apple Music style: Lyrics, Queue)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        currentTab = if (currentTab == NowPlayingTab.LYRICS) NowPlayingTab.PLAYER else NowPlayingTab.LYRICS
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lyrics,
                        contentDescription = "Lyrics",
                        tint = if (currentTab == NowPlayingTab.LYRICS) Color.White else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = {
                        currentTab = if (currentTab == NowPlayingTab.QUEUE) NowPlayingTab.PLAYER else NowPlayingTab.QUEUE
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = "Queue",
                        tint = if (currentTab == NowPlayingTab.QUEUE) Color.White else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    // Sleep Timer Dialog
    if (showSleepTimerDialog) {
        SleepTimerDialog(
            currentMinutes = playbackState.sleepTimerMinutesRemaining,
            onSelectMinutes = { mins ->
                playbackManager.setSleepTimer(mins)
                showSleepTimerDialog = false
            },
            onDismiss = { showSleepTimerDialog = false }
        )
    }

    // Equalizer Dialog
    if (showEqualizerDialog) {
        EqualizerDialog(
            equalizerController = playbackManager.equalizerController,
            onDismiss = { showEqualizerDialog = false }
        )
    }
}

@Composable
fun SleepTimerDialog(
    currentMinutes: Int?,
    onSelectMinutes: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Sleep Timer", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column {
                Text(
                    text = if (currentMinutes != null) "Timer active: $currentMinutes min remaining" else "Turn off playback automatically",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                listOf(15, 30, 45, 60).forEach { mins ->
                    Button(
                        onClick = { onSelectMinutes(mins) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentMinutes == mins) AuraAccentRed else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "$mins Minutes",
                            color = if (currentMinutes == mins) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (currentMinutes != null) {
                    TextButton(
                        onClick = { onSelectMinutes(null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("Turn Off Sleep Timer", color = AuraAccentRed)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EqualizerDialog(
    equalizerController: EqualizerController,
    onDismiss: () -> Unit
) {
    var selectedPreset by remember { mutableStateOf("Flat") }
    val presets = EqualizerController.PRESETS

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Aura Equalizer", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Sound Profiles",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.take(3).forEach { preset ->
                        Button(
                            onClick = {
                                selectedPreset = preset
                                equalizerController.applyPreset(preset)
                            },
                            modifier = Modifier.weight(1f),
                            shape = PillShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedPreset == preset) AuraAccentRed else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = preset,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selectedPreset == preset) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.drop(3).take(3).forEach { preset ->
                        Button(
                            onClick = {
                                selectedPreset = preset
                                equalizerController.applyPreset(preset)
                            },
                            modifier = Modifier.weight(1f),
                            shape = PillShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedPreset == preset) AuraAccentRed else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = preset,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selectedPreset == preset) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(containerColor = AuraAccentRed)
            ) {
                Text("Done", color = Color.White)
            }
        }
    )
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
