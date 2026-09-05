package com.example.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.domain.model.Song
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class RepeatMode {
    OFF, ALL, ONE
}

data class PlaybackUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val queue: List<Song> = emptyList(),
    val queueIndex: Int = -1,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val sleepTimerMinutesRemaining: Int? = null,
    val isConnected: Boolean = false
)

class PlaybackManager(
    private val context: Context,
    private val onSongPlayed: (Long) -> Unit
) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    val equalizerController = EqualizerController()

    private val _uiState = MutableStateFlow(PlaybackUiState())
    val uiState: StateFlow<PlaybackUiState> = _uiState.asStateFlow()

    private var positionUpdateJob: Job? = null
    private var sleepTimerJob: Job? = null

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                controller = controllerFuture?.get()
                setupPlayerListener()
                _uiState.update { it.copy(isConnected = true) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        val player = controller ?: return

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _uiState.update {
                        it.copy(
                            durationMs = player.duration.coerceAtLeast(0L),
                            currentPositionMs = player.currentPosition.coerceAtLeast(0L)
                        )
                    }
                } else if (playbackState == Player.STATE_ENDED) {
                    handleTrackEnded()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val currentQueue = _uiState.value.queue
                val mediaUri = mediaItem?.mediaId
                val index = currentQueue.indexOfFirst { it.contentUri == mediaUri }
                if (index != -1) {
                    val song = currentQueue[index]
                    _uiState.update {
                        it.copy(
                            currentSong = song,
                            queueIndex = index,
                            durationMs = song.durationMs
                        )
                    }
                    onSongPlayed(song.id)
                }
            }
        })
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (isActive) {
                controller?.let { player ->
                    val pos = player.currentPosition.coerceAtLeast(0L)
                    val dur = if (player.duration > 0) player.duration else _uiState.value.currentSong?.durationMs ?: 0L
                    _uiState.update {
                        it.copy(
                            currentPositionMs = pos,
                            durationMs = dur
                        )
                    }
                }
                delay(200)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        controller?.let { player ->
            _uiState.update {
                it.copy(currentPositionMs = player.currentPosition.coerceAtLeast(0L))
            }
        }
    }

    fun playSong(song: Song, queue: List<Song> = emptyList()) {
        val player = controller ?: return

        val newQueue = if (queue.isNotEmpty()) queue else listOf(song)
        val songIndex = newQueue.indexOfFirst { it.id == song.id }.let { if (it >= 0) it else 0 }

        _uiState.update {
            it.copy(
                queue = newQueue,
                queueIndex = songIndex,
                currentSong = song,
                durationMs = song.durationMs,
                currentPositionMs = 0L
            )
        }

        val mediaItems = newQueue.map { s ->
            MediaItem.Builder()
                .setMediaId(s.contentUri)
                .setUri(Uri.parse(s.contentUri))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(s.title)
                        .setArtist(s.artist)
                        .setAlbumTitle(s.album)
                        .setArtworkUri(s.albumArtUri?.let { Uri.parse(it) })
                        .build()
                )
                .build()
        }

        player.setMediaItems(mediaItems, songIndex, 0L)
        player.prepare()
        player.play()

        onSongPlayed(song.id)
    }

    fun togglePlayPause() {
        val player = controller ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            if (_uiState.value.currentSong != null) {
                player.play()
            } else if (_uiState.value.queue.isNotEmpty()) {
                val first = _uiState.value.queue.first()
                playSong(first, _uiState.value.queue)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val player = controller ?: return
        player.seekTo(positionMs)
        _uiState.update { it.copy(currentPositionMs = positionMs) }
    }

    fun next() {
        val player = controller ?: return
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        } else {
            val state = _uiState.value
            if (state.repeatMode == RepeatMode.ALL && state.queue.isNotEmpty()) {
                player.seekTo(0, 0L)
            }
        }
    }

    fun previous() {
        val player = controller ?: return
        if (player.currentPosition > 3000) {
            player.seekTo(0)
            _uiState.update { it.copy(currentPositionMs = 0) }
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        } else {
            player.seekTo(0)
        }
    }

    fun toggleShuffle() {
        val player = controller ?: return
        val newShuffle = !_uiState.value.isShuffleEnabled
        _uiState.update { it.copy(isShuffleEnabled = newShuffle) }
        player.shuffleModeEnabled = newShuffle
    }

    fun toggleRepeat() {
        val player = controller ?: return
        val nextMode = when (_uiState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _uiState.update { it.copy(repeatMode = nextMode) }

        player.repeatMode = when (nextMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
    }

    fun addToQueue(song: Song) {
        val currentQueue = _uiState.value.queue.toMutableList()
        currentQueue.add(song)
        _uiState.update { it.copy(queue = currentQueue) }

        controller?.let { player ->
            val item = MediaItem.Builder()
                .setMediaId(song.contentUri)
                .setUri(Uri.parse(song.contentUri))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setArtworkUri(song.albumArtUri?.let { Uri.parse(it) })
                        .build()
                )
                .build()
            player.addMediaItem(item)
        }
    }

    fun playNext(song: Song) {
        val state = _uiState.value
        val currentQueue = state.queue.toMutableList()
        val insertIndex = (state.queueIndex + 1).coerceIn(0, currentQueue.size)
        currentQueue.add(insertIndex, song)
        _uiState.update { it.copy(queue = currentQueue) }

        controller?.let { player ->
            val item = MediaItem.Builder()
                .setMediaId(song.contentUri)
                .setUri(Uri.parse(song.contentUri))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setArtworkUri(song.albumArtUri?.let { Uri.parse(it) })
                        .build()
                )
                .build()
            player.addMediaItem(insertIndex, item)
        }
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        val currentQueue = _uiState.value.queue.toMutableList()
        if (fromIndex in currentQueue.indices && toIndex in currentQueue.indices) {
            val moved = currentQueue.removeAt(fromIndex)
            currentQueue.add(toIndex, moved)
            val newCurrentIndex = when {
                _uiState.value.queueIndex == fromIndex -> toIndex
                fromIndex < _uiState.value.queueIndex && toIndex >= _uiState.value.queueIndex -> _uiState.value.queueIndex - 1
                fromIndex > _uiState.value.queueIndex && toIndex <= _uiState.value.queueIndex -> _uiState.value.queueIndex + 1
                else -> _uiState.value.queueIndex
            }
            _uiState.update { it.copy(queue = currentQueue, queueIndex = newCurrentIndex) }
            controller?.moveMediaItem(fromIndex, toIndex)
        }
    }

    fun removeFromQueue(index: Int) {
        val currentQueue = _uiState.value.queue.toMutableList()
        if (index in currentQueue.indices) {
            currentQueue.removeAt(index)
            val newCurrentIndex = if (index < _uiState.value.queueIndex) {
                _uiState.value.queueIndex - 1
            } else {
                _uiState.value.queueIndex
            }
            _uiState.update { it.copy(queue = currentQueue, queueIndex = newCurrentIndex) }
            controller?.removeMediaItem(index)
        }
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        if (minutes == null || minutes <= 0) {
            _uiState.update { it.copy(sleepTimerMinutesRemaining = null) }
            return
        }

        _uiState.update { it.copy(sleepTimerMinutesRemaining = minutes) }

        sleepTimerJob = scope.launch {
            var remaining = minutes
            while (remaining > 0) {
                delay(60_000L)
                remaining -= 1
                _uiState.update { it.copy(sleepTimerMinutesRemaining = remaining) }
            }
            controller?.pause()
            _uiState.update { it.copy(sleepTimerMinutesRemaining = null) }
        }
    }

    private fun handleTrackEnded() {
        val state = _uiState.value
        if (state.repeatMode == RepeatMode.ONE) {
            controller?.seekTo(0)
            controller?.play()
        } else if (state.queueIndex < state.queue.size - 1) {
            next()
        } else if (state.repeatMode == RepeatMode.ALL && state.queue.isNotEmpty()) {
            playSong(state.queue.first(), state.queue)
        }
    }

    fun release() {
        stopPositionUpdates()
        sleepTimerJob?.cancel()
        equalizerController.release()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
    }
}
