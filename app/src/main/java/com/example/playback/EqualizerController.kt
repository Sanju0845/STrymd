package com.example.playback

import android.media.audiofx.Equalizer
import android.util.Log

data class EqualizerBand(
    val index: Int,
    val centerFreqHz: Int,
    val levelMilliBels: Int,
    val minMilliBels: Int,
    val maxMilliBels: Int
)

class EqualizerController {

    private var equalizer: Equalizer? = null
    private var isEnabled = true

    companion object {
        private const val TAG = "EqualizerController"
        val PRESETS = listOf("Flat", "Bass Boost", "Electronic", "Rock", "Pop", "Vocal", "Acoustic")
    }

    fun attach(audioSessionId: Int) {
        if (audioSessionId <= 0) return
        try {
            release()
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = isEnabled
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Equalizer with session $audioSessionId", e)
        }
    }

    fun getBands(): List<EqualizerBand> {
        val eq = equalizer ?: return getDefaultBands()
        return try {
            val numBands = eq.numberOfBands.toInt()
            val minLevel = eq.bandLevelRange[0].toInt()
            val maxLevel = eq.bandLevelRange[1].toInt()

            (0 until numBands).map { i ->
                EqualizerBand(
                    index = i,
                    centerFreqHz = eq.getCenterFreq(i.toShort()) / 1000,
                    levelMilliBels = eq.getBandLevel(i.toShort()).toInt(),
                    minMilliBels = minLevel,
                    maxMilliBels = maxLevel
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting bands", e)
            getDefaultBands()
        }
    }

    private fun getDefaultBands(): List<EqualizerBand> {
        return listOf(
            EqualizerBand(0, 60, 0, -1500, 1500),
            EqualizerBand(1, 230, 0, -1500, 1500),
            EqualizerBand(2, 910, 0, -1500, 1500),
            EqualizerBand(3, 3600, 0, -1500, 1500),
            EqualizerBand(4, 14000, 0, -1500, 1500)
        )
    }

    fun setBandLevel(bandIndex: Int, levelMilliBels: Int) {
        try {
            equalizer?.setBandLevel(bandIndex.toShort(), levelMilliBels.toShort())
        } catch (e: Exception) {
            Log.e(TAG, "Error setting band level", e)
        }
    }

    fun applyPreset(presetName: String) {
        val bands = getBands()
        val gains = when (presetName) {
            "Bass Boost" -> listOf(600, 400, 100, 0, 0)
            "Electronic" -> listOf(400, 200, 0, 200, 500)
            "Rock" -> listOf(500, 300, -100, 200, 400)
            "Pop" -> listOf(-100, 200, 500, 300, -100)
            "Vocal" -> listOf(-200, 100, 600, 300, 100)
            "Acoustic" -> listOf(300, 200, 100, 200, 300)
            else -> listOf(0, 0, 0, 0, 0) // Flat
        }

        bands.forEachIndexed { i, band ->
            val gain = gains.getOrElse(i) { 0 }
            setBandLevel(band.index, gain)
        }
    }

    fun release() {
        try {
            equalizer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing Equalizer", e)
        } finally {
            equalizer = null
        }
    }
}
