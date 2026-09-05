package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.AuraPreferences
import com.example.data.preferences.AuraPreferencesRepository
import com.example.data.preferences.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: AuraPreferencesRepository
) : ViewModel() {

    val preferences: StateFlow<AuraPreferences> = preferencesRepository.preferencesFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            AuraPreferences()
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setGaplessPlayback(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setGaplessPlayback(enabled)
        }
    }

    fun setCrossfadeDuration(seconds: Int) {
        viewModelScope.launch {
            preferencesRepository.setCrossfadeDuration(seconds)
        }
    }

    fun setEqualizerPreset(preset: String) {
        viewModelScope.launch {
            preferencesRepository.setEqualizerPreset(preset)
        }
    }

    fun setSleepTimer(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.setSleepTimer(minutes)
        }
    }

    fun setFolderEnabled(folderName: String, enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setFolderEnabled(folderName, enabled)
        }
    }

    class Factory(private val preferencesRepository: AuraPreferencesRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(preferencesRepository) as T
        }
    }
}
