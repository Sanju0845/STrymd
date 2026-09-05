package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.auraDataStore: DataStore<Preferences> by preferencesDataStore(name = "aura_settings")

enum class ThemeMode {
    SYSTEM, DARK, LIGHT
}

data class AuraPreferences(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val isGaplessEnabled: Boolean = true,
    val crossfadeDurationSeconds: Int = 0,
    val equalizerPreset: String = "Flat",
    val sleepTimerMinutes: Int = 0,
    val disabledFolders: Set<String> = emptySet()
)

class AuraPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val GAPLESS_PLAYBACK = booleanPreferencesKey("gapless_playback")
        val CROSSFADE_SECONDS = intPreferencesKey("crossfade_seconds")
        val EQUALIZER_PRESET = stringPreferencesKey("equalizer_preset")
        val SLEEP_TIMER_MINUTES = intPreferencesKey("sleep_timer_minutes")
        val DISABLED_FOLDERS = stringSetPreferencesKey("disabled_folders")
    }

    val preferencesFlow: Flow<AuraPreferences> = context.auraDataStore.data.map { prefs ->
        val themeString = prefs[PreferencesKeys.THEME_MODE] ?: ThemeMode.DARK.name
        val themeMode = try {
            ThemeMode.valueOf(themeString)
        } catch (_: Exception) {
            ThemeMode.DARK
        }

        AuraPreferences(
            themeMode = themeMode,
            isGaplessEnabled = prefs[PreferencesKeys.GAPLESS_PLAYBACK] ?: true,
            crossfadeDurationSeconds = prefs[PreferencesKeys.CROSSFADE_SECONDS] ?: 0,
            equalizerPreset = prefs[PreferencesKeys.EQUALIZER_PRESET] ?: "Flat",
            sleepTimerMinutes = prefs[PreferencesKeys.SLEEP_TIMER_MINUTES] ?: 0,
            disabledFolders = prefs[PreferencesKeys.DISABLED_FOLDERS] ?: emptySet()
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.auraDataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setGaplessPlayback(enabled: Boolean) {
        context.auraDataStore.edit { prefs ->
            prefs[PreferencesKeys.GAPLESS_PLAYBACK] = enabled
        }
    }

    suspend fun setCrossfadeDuration(seconds: Int) {
        context.auraDataStore.edit { prefs ->
            prefs[PreferencesKeys.CROSSFADE_SECONDS] = seconds
        }
    }

    suspend fun setEqualizerPreset(preset: String) {
        context.auraDataStore.edit { prefs ->
            prefs[PreferencesKeys.EQUALIZER_PRESET] = preset
        }
    }

    suspend fun setSleepTimer(minutes: Int) {
        context.auraDataStore.edit { prefs ->
            prefs[PreferencesKeys.SLEEP_TIMER_MINUTES] = minutes
        }
    }

    suspend fun setFolderEnabled(folderName: String, enabled: Boolean) {
        context.auraDataStore.edit { prefs ->
            val current = (prefs[PreferencesKeys.DISABLED_FOLDERS] ?: emptySet()).toMutableSet()
            if (enabled) {
                current.remove(folderName)
            } else {
                current.add(folderName)
            }
            prefs[PreferencesKeys.DISABLED_FOLDERS] = current
        }
    }
}
