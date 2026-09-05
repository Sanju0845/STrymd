package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.AuraPreferences
import com.example.data.preferences.ThemeMode
import com.example.domain.model.Song
import com.example.playback.EqualizerController
import com.example.ui.components.GlassSurface
import com.example.ui.theme.AuraAccentPink
import com.example.ui.theme.AuraAccentRed
import com.example.ui.theme.AuraAccentViolet
import com.example.ui.theme.GlassCardShape
import com.example.ui.theme.PillShape

@Composable
fun SettingsScreen(
    preferences: AuraPreferences,
    totalSongs: Int,
    totalAlbums: Int,
    totalArtists: Int,
    allSongs: List<Song>,
    onThemeChange: (ThemeMode) -> Unit,
    onGaplessChange: (Boolean) -> Unit,
    onCrossfadeChange: (Int) -> Unit,
    onEqualizerPresetChange: (String) -> Unit,
    onSleepTimerChange: (Int) -> Unit,
    onRescan: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val totalDurationMs = remember(allSongs) {
        allSongs.sumOf { it.durationMs }
    }
    val totalHours = totalDurationMs / (1000 * 60 * 60)
    val totalMinutes = (totalDurationMs / (1000 * 60)) % 60

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 140.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        // Appearance / Theme Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSectionHeader(title = "Appearance")
            Spacer(modifier = Modifier.height(8.dp))

            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = GlassCardShape,
                elevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Theme Mode",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeMode.values().forEach { mode ->
                            val isSelected = preferences.themeMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(PillShape)
                                    .background(
                                        if (isSelected) AuraAccentRed else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { onThemeChange(mode) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Audio Engine Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            SettingsSectionHeader(title = "Audio Engine")
            Spacer(modifier = Modifier.height(8.dp))

            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = GlassCardShape,
                elevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Gapless Playback Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gapless Playback",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Seamless transitions without silence between album tracks.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = preferences.isGaplessEnabled,
                            onCheckedChange = onGaplessChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AuraAccentRed
                            ),
                            modifier = Modifier.testTag("gapless_switch")
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    // Crossfade Slider
                    var crossfadeSec by remember(preferences.crossfadeDurationSeconds) {
                        mutableFloatStateOf(preferences.crossfadeDurationSeconds.toFloat())
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Crossfade Duration",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (crossfadeSec.toInt() == 0) "Off" else "${crossfadeSec.toInt()}s",
                                style = MaterialTheme.typography.labelLarge,
                                color = AuraAccentRed
                            )
                        }
                        Slider(
                            value = crossfadeSec,
                            onValueChange = {
                                crossfadeSec = it
                                onCrossfadeChange(it.toInt())
                            },
                            valueRange = 0f..12f,
                            steps = 11,
                            colors = SliderDefaults.colors(
                                thumbColor = AuraAccentRed,
                                activeTrackColor = AuraAccentRed
                            )
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    // Equalizer Presets
                    Text(
                        text = "Equalizer Sound Preset",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val presets = EqualizerController.PRESETS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.take(3).forEach { preset ->
                            val isSelected = preferences.equalizerPreset == preset
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(PillShape)
                                    .background(
                                        if (isSelected) AuraAccentRed else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { onEqualizerPresetChange(preset) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Library Stats Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            SettingsSectionHeader(title = "Local Library Stats")
            Spacer(modifier = Modifier.height(8.dp))

            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = GlassCardShape,
                elevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatBox(value = "$totalSongs", label = "Songs")
                        StatBox(value = "$totalAlbums", label = "Albums")
                        StatBox(value = "$totalArtists", label = "Artists")
                        StatBox(value = "${totalHours}h ${totalMinutes}m", label = "Duration")
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onRescan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("settings_rescan_button"),
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(containerColor = AuraAccentRed.copy(alpha = 0.85f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rescan All Device Audio",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // About STrymd Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            SettingsSectionHeader(title = "About")
            Spacer(modifier = Modifier.height(8.dp))

            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = GlassCardShape,
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(PillShape)
                                .background(AuraAccentRed.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = null,
                                tint = AuraAccentRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "STrymd Music Player",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Version 1.0.0 • Liquid Glass Edition",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "A local-only music player app with liquid glass aesthetics, hardware-accelerated Media3 audio engine, and zero external network tracking.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

@Composable
private fun StatBox(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = AuraAccentRed
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
