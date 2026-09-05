package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entities.FavoriteEntity
import com.example.data.local.entities.PlayHistoryEntity
import com.example.data.local.entities.PlaylistEntity
import com.example.data.local.entities.PlaylistSongCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface AuraDao {

    // --- Favorites ---
    @Query("SELECT songId FROM favorites ORDER BY addedAt DESC")
    fun getAllFavoriteIds(): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    fun isFavorite(songId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun deleteFavorite(songId: Long)

    // --- Playlists ---
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name WHERE id = :id")
    suspend fun updatePlaylistName(id: Long, name: String)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun deletePlaylistSongs(playlistId: Long)

    @Query("SELECT songId FROM playlist_songs WHERE playlistId = :playlistId ORDER BY orderIndex ASC")
    fun getSongIdsForPlaylist(playlistId: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSong(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    // --- History & Stats ---
    @Query("SELECT songId FROM play_history ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentlyPlayedSongIds(limit: Int = 30): Flow<List<Long>>

    @Query("SELECT songId FROM play_history GROUP BY songId ORDER BY SUM(playCount) DESC LIMIT :limit")
    fun getMostPlayedSongIds(limit: Int = 30): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayHistory(history: PlayHistoryEntity)

    @Query("UPDATE play_history SET playCount = playCount + 1, playedAt = :timestamp WHERE songId = :songId")
    suspend fun incrementPlayCount(songId: Long, timestamp: Long): Int

    @Transaction
    suspend fun recordSongPlay(songId: Long) {
        val now = System.currentTimeMillis()
        val updatedRows = incrementPlayCount(songId, now)
        if (updatedRows == 0) {
            insertPlayHistory(PlayHistoryEntity(songId = songId, playedAt = now, playCount = 1))
        }
    }
}
