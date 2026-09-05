package com.example.data.repository

import com.example.data.local.AuraDao
import com.example.data.local.entities.FavoriteEntity
import com.example.data.local.entities.PlaylistEntity
import com.example.data.local.entities.PlaylistSongCrossRef
import com.example.data.mediastore.MediaStoreRepository
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.Genre
import com.example.domain.model.Playlist
import com.example.domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class MusicRepository(
    private val mediaStoreRepository: MediaStoreRepository,
    private val auraDao: AuraDao
) {

    val allSongs: Flow<List<Song>> = combine(
        mediaStoreRepository.songsFlow,
        auraDao.getAllFavoriteIds()
    ) { songs, favoriteIds ->
        val favSet = favoriteIds.toSet()
        songs.map { song ->
            song.copy(isFavorite = favSet.contains(song.id))
        }
    }

    val favoriteSongs: Flow<List<Song>> = allSongs.map { songs ->
        songs.filter { it.isFavorite }
    }

    val recentlyAddedSongs: Flow<List<Song>> = allSongs.map { songs ->
        songs.sortedByDescending { it.dateAdded }.take(25)
    }

    val recentlyPlayedSongs: Flow<List<Song>> = combine(
        allSongs,
        auraDao.getRecentlyPlayedSongIds(30)
    ) { songs, recentIds ->
        val songMap = songs.associateBy { it.id }
        recentIds.mapNotNull { songMap[it] }
    }

    val mostPlayedSongs: Flow<List<Song>> = combine(
        allSongs,
        auraDao.getMostPlayedSongIds(30)
    ) { songs, mostPlayedIds ->
        val songMap = songs.associateBy { it.id }
        mostPlayedIds.mapNotNull { songMap[it] }
    }

    val playlists: Flow<List<Playlist>> = combine(
        auraDao.getAllPlaylists(),
        allSongs
    ) { playlistEntities, songs ->
        playlistEntities.map { entity ->
            Playlist(
                id = entity.id,
                name = entity.name,
                createdAt = entity.createdAt,
                songCount = 0, // updated when querying details
                coverArtUri = null,
                isSmartPlaylist = entity.isSmartPlaylist
            )
        }
    }

    fun getPlaylistSongs(playlistId: Long): Flow<List<Song>> {
        return combine(
            auraDao.getSongIdsForPlaylist(playlistId),
            allSongs
        ) { songIds, songs ->
            val songMap = songs.associateBy { it.id }
            songIds.mapNotNull { songMap[it] }
        }
    }

    suspend fun toggleFavorite(songId: Long, currentIsFavorite: Boolean) {
        if (currentIsFavorite) {
            auraDao.deleteFavorite(songId)
        } else {
            auraDao.insertFavorite(FavoriteEntity(songId = songId))
        }
    }

    suspend fun recordSongPlay(songId: Long) {
        auraDao.recordSongPlay(songId)
    }

    suspend fun createPlaylist(name: String): Long {
        return auraDao.insertPlaylist(PlaylistEntity(name = name))
    }

    suspend fun deletePlaylist(playlistId: Long) {
        auraDao.deletePlaylistSongs(playlistId)
        auraDao.deletePlaylist(playlistId)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        auraDao.insertPlaylistSong(PlaylistSongCrossRef(playlistId = playlistId, songId = songId))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        auraDao.removeSongFromPlaylist(playlistId, songId)
    }

    suspend fun rescan(): List<Song> {
        return mediaStoreRepository.forceRescan()
    }

    fun getAlbums(songs: List<Song>): List<Album> = mediaStoreRepository.buildAlbums(songs)

    fun getArtists(songs: List<Song>): List<Artist> = mediaStoreRepository.buildArtists(songs)

    fun getGenres(songs: List<Song>): List<Genre> = mediaStoreRepository.buildGenres(songs)

    fun getFolders(songs: List<Song>): List<String> = mediaStoreRepository.buildFolders(songs)
}
