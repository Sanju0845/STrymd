package com.example.ui.glass

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * The three quality tiers for liquid glass rendering.
 *
 * LIQUID  -> API 33+ (Android 13+): Full AGSL runtime shader with SDF-based lens
 *           refraction, specular rim, chromatic dispersion, RenderEffect blur chain.
 *
 * FROSTED -> API 31-32 (Android 12): RenderEffect blur (GPU) + tinted overlay +
 *           border highlight. No refraction, still buttery smooth.
 *
 * LEGACY  -> API 26-30: Software blur via Cloudy's CPU path +
 *           semi-transparent overlay. Frosted glass appearance.
 */
enum class GlassRenderTier {
    LIQUID,   // API 33+
    FROSTED,  // API 31-32
    LEGACY    // API 26-30
}

fun resolveGlassTier(): GlassRenderTier = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> GlassRenderTier.LIQUID
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S        -> GlassRenderTier.FROSTED
    else                                                   -> GlassRenderTier.LEGACY
}

@Composable
fun rememberGlassTier(): GlassRenderTier = remember { resolveGlassTier() }
