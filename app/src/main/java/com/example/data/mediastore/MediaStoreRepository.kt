package com.example.data.mediastore

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.Genre
import com.example.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

class MediaStoreRepository(private val context: Context) {

    companion object {
        private const val TAG = "MediaStoreRepository"
    }

    private val rescanTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val songsFlow: Flow<List<Song>> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                trySend(scanAudioFiles())
            }
        }

        try {
            context.contentResolver.registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true,
                observer
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error registering MediaStore observer", e)
        }

        // Initial scan
        trySend(scanAudioFiles())

        awaitClose {
            try {
                context.contentResolver.unregisterContentObserver(observer)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering MediaStore observer", e)
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun forceRescan(): List<Song> = withContext(Dispatchers.IO) {
        val songs = scanAudioFiles()
        rescanTrigger.tryEmit(Unit)
        songs
    }

    fun scanAudioFiles(): List<Song> {
        val scannedSongs = mutableListOf<Song>()
        val seenIds = mutableSetOf<Long>()

        // Scan all storage volumes (Internal, External, SD Cards) to capture ALL music
        val urisToScan = mutableListOf<Uri>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val volumeNames = MediaStore.getExternalVolumeNames(context)
                    for (vol in volumeNames) {
                        add(MediaStore.Audio.Media.getContentUri(vol))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting external volume names", e)
                }
            }
            if (isEmpty()) {
                add(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
            }
            add(MediaStore.Audio.Media.INTERNAL_CONTENT_URI)
        }.distinct()

        val projection = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)
                add(MediaStore.Audio.Media.RELATIVE_PATH)
            }
        }.toTypedArray()

        // DO NOT filter by duration or is_music so all files (WhatsApp, Downloads, Bluetooth, recordings, etc.) appear
        val selection = null
        val selectionArgs = null
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        for (contentUri in urisToScan) {
            try {
                context.contentResolver.query(
                    contentUri,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                    val artistIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
                    val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                    val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                    val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                    val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                    val bucketColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        cursor.getColumnIndex(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)
                    } else -1
                    val relativePathColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
                    } else -1

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        if (seenIds.contains(id)) continue
                        seenIds.add(id)

                        val rawTitle = cursor.getString(titleColumn)
                        val rawArtist = cursor.getString(artistColumn)
                        val rawAlbum = cursor.getString(albumColumn)
                        val duration = cursor.getLong(durationColumn)
                        val albumId = cursor.getLong(albumIdColumn)
                        val artistId = cursor.getLong(artistIdColumn)
                        val track = cursor.getInt(trackColumn)
                        val year = cursor.getInt(yearColumn)
                        val dateAdded = cursor.getLong(dateAddedColumn)

                        val dataPath = if (dataColumn >= 0) cursor.getString(dataColumn) ?: "" else ""
                        val bucketName = if (bucketColumn >= 0) cursor.getString(bucketColumn) else null
                        val relativePath = if (relativePathColumn >= 0) cursor.getString(relativePathColumn) else null

                        val title = if (rawTitle.isNullOrBlank()) {
                            if (dataPath.isNotEmpty()) File(dataPath).nameWithoutExtension else "Audio Track"
                        } else rawTitle

                        val artist = if (rawArtist.isNullOrBlank() || rawArtist.contains("<unknown>", ignoreCase = true)) {
                            "Unknown Artist"
                        } else rawArtist

                        val album = if (rawAlbum.isNullOrBlank() || rawAlbum.contains("<unknown>", ignoreCase = true)) {
                            "Unknown Album"
                        } else rawAlbum

                        val trackUri = ContentUris.withAppendedId(contentUri, id).toString()

                        val albumArtUri = ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        ).toString()

                        // Determine folder from bucket, relative path, or file path
                        val folderName = when {
                            !bucketName.isNullOrBlank() -> bucketName
                            !relativePath.isNullOrBlank() -> relativePath.trimEnd('/').substringAfterLast('/')
                            dataPath.isNotEmpty() -> File(dataPath).parentFile?.name ?: "Music"
                            else -> "Device Audio"
                        }

                        scannedSongs.add(
                            Song(
                                id = id,
                                title = title,
                                artist = artist,
                                album = album,
                                durationMs = if (duration > 0) duration else 180000L,
                                contentUri = trackUri,
                                albumArtUri = albumArtUri,
                                albumId = albumId,
                                artistId = artistId,
                                trackNumber = track,
                                year = year,
                                genre = null,
                                folderName = folderName,
                                dateAdded = dateAdded
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error querying MediaStore at $contentUri", e)
            }
        }

        // Return all user songs, or demo songs if device has no audio files yet
        return if (scannedSongs.isEmpty()) {
            DemoMusicProvider.getDemoSongs()
        } else {
            scannedSongs
        }
    }

    fun buildAlbums(songs: List<Song>): List<Album> {
        return songs.groupBy { it.albumId }.map { (albumId, albumSongs) ->
            val first = albumSongs.first()
            Album(
                id = albumId,
                title = first.album,
                artist = first.artist,
                artUri = first.albumArtUri,
                year = first.year,
                songCount = albumSongs.size
            )
        }.sortedBy { it.title }
    }

    fun buildArtists(songs: List<Song>): List<Artist> {
        return songs.groupBy { it.artist.lowercase().trim() }.map { (_, artistSongs) ->
            val first = artistSongs.first()
            val albumCount = artistSongs.map { it.albumId }.distinct().size
            Artist(
                id = first.artistId,
                name = first.artist,
                songCount = artistSongs.size,
                albumCount = albumCount
            )
        }.sortedBy { it.name }
    }

    fun buildGenres(songs: List<Song>): List<Genre> {
        val defaultGenres = listOf(
            Genre(1L, "Electronic", songs.filter { it.title.contains("Pulse", true) || it.title.contains("Synth", true) }.size.coerceAtLeast(1)),
            Genre(2L, "Ambient", songs.filter { it.title.contains("Aura", true) || it.title.contains("Echo", true) }.size.coerceAtLeast(1)),
            Genre(3L, "Chillout", songs.filter { it.title.contains("Velvet", true) || it.title.contains("Breeze", true) }.size.coerceAtLeast(1)),
            Genre(4L, "Acoustic", songs.filter { it.title.contains("Dawn", true) || it.title.contains("Horizon", true) }.size.coerceAtLeast(1)),
            Genre(5L, "Soundtrack", songs.size.coerceAtLeast(1))
        )
        return defaultGenres
    }

    fun buildFolders(songs: List<Song>): List<String> {
        return songs.map { it.folderName }.distinct().sorted()
    }
}
