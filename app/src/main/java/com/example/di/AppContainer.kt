package com.example.di

import android.content.Context
import com.example.data.local.AuraDatabase
import com.example.data.mediastore.MediaStoreRepository
import com.example.data.preferences.AuraPreferencesRepository
import com.example.data.repository.MusicRepository
import com.example.playback.PlaybackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppContainer(val context: Context) {

    private val database = AuraDatabase.getInstance(context)
    val auraDao = database.auraDao()

    val mediaStoreRepository = MediaStoreRepository(context)
    val preferencesRepository = AuraPreferencesRepository(context)
    val musicRepository = MusicRepository(mediaStoreRepository, auraDao, preferencesRepository)

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    val playbackManager: PlaybackManager by lazy {
        PlaybackManager(
            context = context,
            onSongPlayed = { songId ->
                applicationScope.launch {
                    musicRepository.recordSongPlay(songId)
                }
            }
        )
    }
}
