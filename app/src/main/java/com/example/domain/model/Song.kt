package com.example.domain.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUri: String,
    val albumArtUri: String? = null,
    val albumId: Long = 0L,
    val artistId: Long = 0L,
    val trackNumber: Int = 0,
    val year: Int = 0,
    val genre: String? = null,
    val folderName: String = "",
    val dateAdded: Long = 0L,
    val isFavorite: Boolean = false,
    val lyrics: String? = null
)
