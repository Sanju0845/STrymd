package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.MusicRepository
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.Genre
import com.example.domain.model.Playlist
import com.example.domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchResults(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList()
)

class LibraryViewModel(
    private val musicRepository: MusicRepository
) : ViewModel() {

    val allSongs: StateFlow<List<Song>> = musicRepository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = musicRepository.favoriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyAddedSongs: StateFlow<List<Song>> = musicRepository.recentlyAddedSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayedSongs: StateFlow<List<Song>> = musicRepository.recentlyPlayedSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostPlayedSongs: StateFlow<List<Song>> = musicRepository.mostPlayedSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = musicRepository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<Album>> = allSongs.map { songs ->
        musicRepository.getAlbums(songs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artists: StateFlow<List<Artist>> = allSongs.map { songs ->
        musicRepository.getArtists(songs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val genres: StateFlow<List<Genre>> = allSongs.map { songs ->
        musicRepository.getGenres(songs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<String>> = allSongs.map { songs ->
        musicRepository.getFolders(songs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _recentSearches = MutableStateFlow(listOf("Midnight Horizon", "Solaris Echo", "Synthwave"))
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    val searchResults: StateFlow<SearchResults> = combine(
        allSongs,
        albums,
        artists,
        searchQuery
    ) { songs, albumList, artistList, query ->
        if (query.isBlank()) {
            SearchResults()
        } else {
            val q = query.trim().lowercase()
            val matchedSongs = songs.filter {
                it.title.lowercase().contains(q) ||
                it.artist.lowercase().contains(q) ||
                it.album.lowercase().contains(q)
            }
            val matchedAlbums = albumList.filter {
                it.title.lowercase().contains(q) || it.artist.lowercase().contains(q)
            }
            val matchedArtists = artistList.filter {
                it.name.lowercase().contains(q)
            }
            SearchResults(matchedSongs, matchedAlbums, matchedArtists)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResults())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank() && query.length > 2) {
            val current = _recentSearches.value.toMutableList()
            current.remove(query)
            current.add(0, query)
            _recentSearches.value = current.take(6)
        }
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    fun rescan() {
        viewModelScope.launch {
            musicRepository.rescan()
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(song.id, song.isFavorite)
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                musicRepository.createPlaylist(name.trim())
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            musicRepository.deletePlaylist(playlistId)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            musicRepository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            musicRepository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun getPlaylistSongs(playlistId: Long): Flow<List<Song>> {
        return musicRepository.getPlaylistSongs(playlistId)
    }

    class Factory(private val musicRepository: MusicRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LibraryViewModel(musicRepository) as T
        }
    }
}
