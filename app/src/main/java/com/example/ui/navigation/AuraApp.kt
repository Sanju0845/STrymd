package com.example.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.AuraApplication
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.Playlist
import com.example.domain.model.Song
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import com.example.data.preferences.ThemeMode
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import com.example.ui.home.NewScreen
import com.example.ui.home.OnlineMusicScreen
import com.example.ui.glass.GlassMiniPlayer
import com.example.ui.glass.GlassNavBar
import com.example.ui.glass.GlassNavItem
import com.example.ui.home.HomeScreen
import com.example.ui.library.DetailScreen
import com.example.ui.library.LibraryScreen
import com.example.ui.nowplaying.NowPlayingScreen
import com.example.ui.permissions.PermissionScreen
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.example.ui.search.SearchScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.AuraTheme
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

sealed class ActiveScreen {
    object Main : ActiveScreen()
    object Settings : ActiveScreen()
    data class AlbumDetail(val album: Album) : ActiveScreen()
    data class ArtistDetail(val artist: Artist) : ActiveScreen()
    data class PlaylistDetail(val playlist: Playlist) : ActiveScreen()
    data class FolderDetail(val folderName: String) : ActiveScreen()
}

@Composable
fun AuraApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val container = (context.applicationContext as AuraApplication).container

    val libraryViewModel: LibraryViewModel = viewModel(
        factory = LibraryViewModel.Factory(container.musicRepository)
    )
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(container.preferencesRepository)
    )

    val playbackManager = container.playbackManager
    val playbackState by playbackManager.uiState.collectAsState()
    val preferences by settingsViewModel.preferences.collectAsState()

    val allSongs by libraryViewModel.allSongs.collectAsState()
    val albums by libraryViewModel.albums.collectAsState()
    val artists by libraryViewModel.artists.collectAsState()
    val playlists by libraryViewModel.playlists.collectAsState()
    val genres by libraryViewModel.genres.collectAsState()
    val folders by libraryViewModel.folders.collectAsState()

    val favoriteSongs by libraryViewModel.favoriteSongs.collectAsState()
    val recentlyAdded by libraryViewModel.recentlyAddedSongs.collectAsState()
    val recentlyPlayed by libraryViewModel.recentlyPlayedSongs.collectAsState()
    val mostPlayed by libraryViewModel.mostPlayedSongs.collectAsState()

    val searchQuery by libraryViewModel.searchQuery.collectAsState()
    val searchResults by libraryViewModel.searchResults.collectAsState()
    val recentSearches by libraryViewModel.recentSearches.collectAsState()

    // Permission handling
    val requiredPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, requiredPermission) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionSkipped by remember { mutableStateOf(false) }

    // Rescan audio files whenever app resumes (e.g., returning from downloads/WhatsApp)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = ContextCompat.checkSelfPermission(context, requiredPermission) == PackageManager.PERMISSION_GRANTED
                if (granted && !hasPermission) {
                    hasPermission = true
                }
                libraryViewModel.rescan()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 5 })

    val hazeState = remember { HazeState() }
    val isDark = preferences.themeMode != ThemeMode.LIGHT

    var activeScreen by remember { mutableStateOf<ActiveScreen>(ActiveScreen.Main) }
    var isNowPlayingExpanded by remember { mutableStateOf(false) }

    AuraTheme(themeMode = preferences.themeMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (!hasPermission && !permissionSkipped) {
                PermissionScreen(
                    onPermissionGranted = {
                        hasPermission = true
                        permissionSkipped = true
                        libraryViewModel.rescan()
                    }
                )
            } else {
                // Main Navigation Container with silky slide transitions
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    AnimatedContent(
                        targetState = activeScreen,
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(state = hazeState),
                        transitionSpec = {
                            if (targetState is ActiveScreen.Main) {
                                slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                            } else {
                                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                            }
                        },
                        label = "active_screen_transition"
                    ) { screen ->
                        when (screen) {
                            is ActiveScreen.Main -> {
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxSize(),
                                    beyondViewportPageCount = 1
                                ) { page ->
                                    when (page) {
                                        0 -> HomeScreen(
                                            allSongs = allSongs,
                                            recentlyPlayed = recentlyPlayed,
                                            recentlyAdded = recentlyAdded,
                                            mostPlayed = mostPlayed,
                                            favorites = favoriteSongs,
                                            albums = albums,
                                            onPlaySong = { song, queue ->
                                                playbackManager.playSong(song, queue)
                                            },
                                            onNavigateToLibraryTab = { _ ->
                                                coroutineScope.launch {
                                                    pagerState.animateScrollToPage(3)
                                                }
                                            },
                                            onNavigateToSettings = {
                                                activeScreen = ActiveScreen.Settings
                                            }
                                        )

                                        1 -> NewScreen(
                                            allSongs = allSongs,
                                            recentlyAdded = recentlyAdded,
                                            albums = albums,
                                            onPlaySong = { song, queue ->
                                                playbackManager.playSong(song, queue)
                                            },
                                            onAlbumSelected = { activeScreen = ActiveScreen.AlbumDetail(it) },
                                            onNavigateToLibrary = {
                                                coroutineScope.launch {
                                                    pagerState.animateScrollToPage(3)
                                                }
                                            }
                                        )

                                        2 -> OnlineMusicScreen(
                                            onPlaySong = { song, queue ->
                                                playbackManager.playSong(song, queue)
                                            }
                                        )

                                        3 -> LibraryScreen(
                                            songs = allSongs,
                                            albums = albums,
                                            artists = artists,
                                            playlists = playlists,
                                            genres = genres,
                                            folders = folders,
                                            onPlaySong = { song, queue ->
                                                playbackManager.playSong(song, queue)
                                            },
                                            onAlbumSelected = { activeScreen = ActiveScreen.AlbumDetail(it) },
                                            onArtistSelected = { activeScreen = ActiveScreen.ArtistDetail(it) },
                                            onPlaylistSelected = { activeScreen = ActiveScreen.PlaylistDetail(it) },
                                            onFolderSelected = { activeScreen = ActiveScreen.FolderDetail(it) },
                                            onCreatePlaylist = { libraryViewModel.createPlaylist(it) },
                                            onRescan = { libraryViewModel.rescan() },
                                            onNavigateToSettings = {
                                                activeScreen = ActiveScreen.Settings
                                            }
                                        )

                                        4 -> SearchScreen(
                                            query = searchQuery,
                                            searchResults = searchResults,
                                            recentSearches = recentSearches,
                                            onQueryChange = { libraryViewModel.updateSearchQuery(it) },
                                            onClearQuery = { libraryViewModel.clearSearchQuery() },
                                            onPlaySong = { song, queue ->
                                                playbackManager.playSong(song, queue)
                                            },
                                            onAlbumSelected = { activeScreen = ActiveScreen.AlbumDetail(it) },
                                            onArtistSelected = { activeScreen = ActiveScreen.ArtistDetail(it) }
                                        )
                                    }
                                }
                            }

                            is ActiveScreen.Settings -> {
                                SettingsScreen(
                                    preferences = preferences,
                                    totalSongs = allSongs.size,
                                    totalAlbums = albums.size,
                                    totalArtists = artists.size,
                                    allSongs = allSongs,
                                    onThemeChange = { settingsViewModel.setThemeMode(it) },
                                    onGaplessChange = { settingsViewModel.setGaplessPlayback(it) },
                                    onCrossfadeChange = { settingsViewModel.setCrossfadeDuration(it) },
                                    onEqualizerPresetChange = { settingsViewModel.setEqualizerPreset(it) },
                                    onSleepTimerChange = { settingsViewModel.setSleepTimer(it) },
                                    onFolderToggle = { folder, enabled -> settingsViewModel.setFolderEnabled(folder, enabled) },
                                    onRescan = { libraryViewModel.rescan() },
                                    onBack = { activeScreen = ActiveScreen.Main }
                                )
                            }

                            is ActiveScreen.AlbumDetail -> {
                                val albumSongs = allSongs.filter { it.albumId == screen.album.id }
                                DetailScreen(
                                    title = screen.album.title,
                                    subtitle = screen.album.artist,
                                    artUri = screen.album.artUri,
                                    songs = albumSongs,
                                    playlists = playlists,
                                    onBack = { activeScreen = ActiveScreen.Main },
                                    onPlaySong = { song, queue -> playbackManager.playSong(song, queue) },
                                    onShuffleAll = {
                                        if (albumSongs.isNotEmpty()) {
                                            val shuffled = albumSongs.shuffled()
                                            playbackManager.playSong(shuffled.first(), shuffled)
                                        }
                                    },
                                    onToggleFavorite = { libraryViewModel.toggleFavorite(it) },
                                    onPlayNext = { playbackManager.playNext(it) },
                                    onAddToQueue = { playbackManager.addToQueue(it) },
                                    onCreatePlaylist = { libraryViewModel.createPlaylist(it) },
                                    onAddSongToPlaylist = { pid, sid -> libraryViewModel.addSongToPlaylist(pid, sid) }
                                )
                            }

                            is ActiveScreen.ArtistDetail -> {
                                val artistSongs = allSongs.filter { it.artist.equals(screen.artist.name, ignoreCase = true) }
                                DetailScreen(
                                    title = screen.artist.name,
                                    subtitle = "${screen.artist.songCount} Songs • ${screen.artist.albumCount} Albums",
                                    artUri = artistSongs.firstOrNull()?.albumArtUri,
                                    songs = artistSongs,
                                    playlists = playlists,
                                    onBack = { activeScreen = ActiveScreen.Main },
                                    onPlaySong = { song, queue -> playbackManager.playSong(song, queue) },
                                    onShuffleAll = {
                                        if (artistSongs.isNotEmpty()) {
                                            val shuffled = artistSongs.shuffled()
                                            playbackManager.playSong(shuffled.first(), shuffled)
                                        }
                                    },
                                    onToggleFavorite = { libraryViewModel.toggleFavorite(it) },
                                    onPlayNext = { playbackManager.playNext(it) },
                                    onAddToQueue = { playbackManager.addToQueue(it) },
                                    onCreatePlaylist = { libraryViewModel.createPlaylist(it) },
                                    onAddSongToPlaylist = { pid, sid -> libraryViewModel.addSongToPlaylist(pid, sid) }
                                )
                            }

                            is ActiveScreen.PlaylistDetail -> {
                                val playlistSongs by libraryViewModel.getPlaylistSongs(screen.playlist.id).collectAsState(initial = emptyList<Song>())
                                DetailScreen(
                                    title = screen.playlist.name,
                                    subtitle = "${playlistSongs.size} Songs",
                                    artUri = playlistSongs.firstOrNull()?.albumArtUri,
                                    songs = playlistSongs,
                                    playlists = playlists,
                                    onBack = { activeScreen = ActiveScreen.Main },
                                    onPlaySong = { song, queue -> playbackManager.playSong(song, queue) },
                                    onShuffleAll = {
                                        if (playlistSongs.isNotEmpty()) {
                                            val shuffled = playlistSongs.shuffled()
                                            playbackManager.playSong(shuffled.first(), shuffled)
                                        }
                                    },
                                    onToggleFavorite = { libraryViewModel.toggleFavorite(it) },
                                    onPlayNext = { playbackManager.playNext(it) },
                                    onAddToQueue = { playbackManager.addToQueue(it) },
                                    onCreatePlaylist = { libraryViewModel.createPlaylist(it) },
                                    onAddSongToPlaylist = { pid, sid -> libraryViewModel.addSongToPlaylist(pid, sid) }
                                )
                            }

                            is ActiveScreen.FolderDetail -> {
                                val folderSongs = allSongs.filter { it.folderName == screen.folderName }
                                DetailScreen(
                                    title = screen.folderName,
                                    subtitle = "${folderSongs.size} Songs in folder",
                                    artUri = folderSongs.firstOrNull()?.albumArtUri,
                                    songs = folderSongs,
                                    playlists = playlists,
                                    onBack = { activeScreen = ActiveScreen.Main },
                                    onPlaySong = { song, queue -> playbackManager.playSong(song, queue) },
                                    onShuffleAll = {
                                        if (folderSongs.isNotEmpty()) {
                                            val shuffled = folderSongs.shuffled()
                                            playbackManager.playSong(shuffled.first(), shuffled)
                                        }
                                    },
                                    onToggleFavorite = { libraryViewModel.toggleFavorite(it) },
                                    onPlayNext = { playbackManager.playNext(it) },
                                    onAddToQueue = { playbackManager.addToQueue(it) },
                                    onCreatePlaylist = { libraryViewModel.createPlaylist(it) },
                                    onAddSongToPlaylist = { pid, sid -> libraryViewModel.addSongToPlaylist(pid, sid) }
                                )
                            }
                        }
                    }

                    // Floating Bottom Controls: Ambient Glass Blur Scrim + Glass Mini Player + Glass Nav Bar
                    if (!isNowPlayingExpanded) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                        ) {
                            // Ambient backdrop blur scrim behind the entire bottom dock area
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(
                                        Brush.verticalGradient(
                                            0f to Color.Transparent,
                                            0.25f to (if (isDark) Color(0xFF030712).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.4f)),
                                            1f to (if (isDark) Color(0xFF030712).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f))
                                        )
                                    )
                                    .hazeEffect(
                                        state = hazeState,
                                        style = HazeStyle(
                                            blurRadius = 26.dp,
                                            tint = if (isDark) HazeTint(Color(0xFF030712).copy(alpha = 0.2f))
                                            else HazeTint(Color.White.copy(alpha = 0.3f))
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(bottom = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Glass Mini Player if a song is loaded
                                AnimatedVisibility(
                                    visible = playbackState.currentSong != null,
                                    enter = slideInVertically { it } + fadeIn(),
                                    exit = slideOutVertically { it } + fadeOut()
                                ) {
                                    val song = playbackState.currentSong
                                    if (song != null) {
                                        val progress = if (playbackState.durationMs > 0) {
                                            (playbackState.currentPositionMs.toFloat() / playbackState.durationMs).coerceIn(0f, 1f)
                                        } else 0f
                                        GlassMiniPlayer(
                                            title = song.title,
                                            artist = song.artist,
                                            isPlaying = playbackState.isPlaying,
                                            progress = progress,
                                            artUri = song.albumArtUri,
                                            hazeState = hazeState,
                                            isDarkTheme = isDark,
                                            onPlayPause = { playbackManager.togglePlayPause() },
                                            onNext = { playbackManager.next() },
                                            onTap = { isNowPlayingExpanded = true }
                                        )
                                    }
                                }

                                // Apple Music Floating Glass Nav Bar: Left capsule (Home, New, Radio, Library) + Right circle (Search)
                                val isSearchSelected = (pagerState.currentPage == 4 && activeScreen is ActiveScreen.Main)
                                GlassNavBar(
                                    selectedIndex = pagerState.currentPage,
                                    isSearchSelected = isSearchSelected,
                                    hazeState = hazeState,
                                    isDarkTheme = isDark,
                                    onItemSelected = { index ->
                                        activeScreen = ActiveScreen.Main
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    onSearchClick = {
                                        activeScreen = ActiveScreen.Main
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(4)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Expanded Now Playing Screen Modal
                AnimatedVisibility(
                    visible = isNowPlayingExpanded && playbackState.currentSong != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    NowPlayingScreen(
                        playbackManager = playbackManager,
                        playbackState = playbackState,
                        onToggleFavorite = { libraryViewModel.toggleFavorite(it) },
                        onDismiss = { isNowPlayingExpanded = false }
                    )
                }
            }
        }
    }

    BackHandler(enabled = isNowPlayingExpanded || activeScreen !is ActiveScreen.Main) {
        if (isNowPlayingExpanded) {
            isNowPlayingExpanded = false
        } else if (activeScreen !is ActiveScreen.Main) {
            activeScreen = ActiveScreen.Main
        }
    }
}
