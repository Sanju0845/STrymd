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
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })

    val hazeState = remember { HazeState() }
    val isDark = preferences.themeMode != ThemeMode.LIGHT

    val navItems = remember {
        listOf(
            GlassNavItem(label = "Home", icon = Icons.Rounded.Home),
            GlassNavItem(label = "Library", icon = Icons.Rounded.LibraryMusic),
            GlassNavItem(label = "Search", icon = Icons.Rounded.Search),
            GlassNavItem(label = "Settings", icon = Icons.Rounded.Settings)
        )
    }

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
                                                    pagerState.animateScrollToPage(1)
                                                }
                                            }
                                        )

                                        1 -> LibraryScreen(
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
                                            onRescan = { libraryViewModel.rescan() }
                                        )

                                        2 -> SearchScreen(
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

                                        3 -> SettingsScreen(
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
                                            onRescan = { libraryViewModel.rescan() }
                                        )
                                    }
                                }
                            }

                            is ActiveScreen.AlbumDetail -> {
                                val albumSongs = allSongs.filter { it.albumId == screen.album.id }
                                DetailScreen(
                                    title = screen.album.title,
                                    subtitle = screen.album.artist,
                                    artUri = screen.album.artUri,
                                    songs = albumSongs,
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
                                    onAddToQueue = { playbackManager.addToQueue(it) }
                                )
                            }

                            is ActiveScreen.ArtistDetail -> {
                                val artistSongs = allSongs.filter { it.artist.equals(screen.artist.name, ignoreCase = true) }
                                DetailScreen(
                                    title = screen.artist.name,
                                    subtitle = "${screen.artist.songCount} Songs • ${screen.artist.albumCount} Albums",
                                    artUri = artistSongs.firstOrNull()?.albumArtUri,
                                    songs = artistSongs,
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
                                    onAddToQueue = { playbackManager.addToQueue(it) }
                                )
                            }

                            is ActiveScreen.PlaylistDetail -> {
                                val playlistSongs by libraryViewModel.getPlaylistSongs(screen.playlist.id).collectAsState(initial = emptyList<Song>())
                                DetailScreen(
                                    title = screen.playlist.name,
                                    subtitle = "${playlistSongs.size} Songs",
                                    artUri = playlistSongs.firstOrNull()?.albumArtUri,
                                    songs = playlistSongs,
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
                                    onAddToQueue = { playbackManager.addToQueue(it) }
                                )
                            }

                            is ActiveScreen.FolderDetail -> {
                                val folderSongs = allSongs.filter { it.folderName == screen.folderName }
                                DetailScreen(
                                    title = screen.folderName,
                                    subtitle = "${folderSongs.size} Songs in folder",
                                    artUri = folderSongs.firstOrNull()?.albumArtUri,
                                    songs = folderSongs,
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
                                    onAddToQueue = { playbackManager.addToQueue(it) }
                                )
                            }
                        }
                    }

                    // Floating Bottom Controls: Glass Mini Player + Glass Nav Bar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
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

                            // Floating Glass Nav Bar
                            GlassNavBar(
                                items = navItems,
                                selectedIndex = pagerState.currentPage,
                                hazeState = hazeState,
                                isDarkTheme = isDark,
                                onItemSelected = { index ->
                                    activeScreen = ActiveScreen.Main
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                            )
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
