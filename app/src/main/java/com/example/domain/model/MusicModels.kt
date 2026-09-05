package com.example.domain.model

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val artUri: String? = null,
    val songCount: Int = 0,
    val year: Int = 0
)

data class Artist(
    val id: Long,
    val name: String,
    val songCount: Int = 0,
    val albumCount: Int = 0
)

data class Genre(
    val id: Long,
    val name: String,
    val songCount: Int = 0
)

data class Playlist(
    val id: Long,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val songCount: Int = 0,
    val coverArtUri: String? = null,
    val isSmartPlaylist: Boolean = false
)
