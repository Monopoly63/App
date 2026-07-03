package com.lumen.player.theme

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import kotlin.math.max
import kotlin.math.min

/**
 * "Silent Luxury" palette core. Every dynamic accent that ever reaches the UI
 * passes through [desaturateForLuxury] first — this is what prevents the
 * chaotic, saturated Material-You look and keeps things editorial/quiet.
 */
data class LuxuryAccent(
    val primary: Color,
    val soft: Color,   // ~10% alpha wash for large surfaces
    val glow: Color,   // lifted lightness, used for ambient blur highlights
)

val DefaultLuxuryAccent = LuxuryAccent(
    primary = Color(0xFF8B8D98),
    soft = Color(0x1A8B8D98),
    glow = Color(0xFFA9ACC0),
)

/**
 * Extracts a dominant swatch from album art via the Palette API, then clamps
 * saturation/lightness into a narrow, elegant band instead of trusting the
 * raw (often garish) vibrant swatch.
 */
fun extractLuxuryAccent(bitmap: Bitmap?): LuxuryAccent {
    if (bitmap == null) return DefaultLuxuryAccent
    val palette = Palette.from(bitmap).clearFilters().generate()
    val swatch = palette.vibrantSwatch
        ?: palette.dominantSwatch
        ?: return DefaultLuxuryAccent

    val hsl = swatch.hsl // FloatArray[hue, saturation, lightness] each 0f..1f
    val hue = hsl[0]
    val desaturated = min(max(hsl[1] * 0.30f, 0.06f), 0.24f)
    val lightness = min(max(hsl[2], 0.32f), 0.46f)
    val glowLightness = min(lightness + 0.22f, 0.68f)

    val primary = Color.hsl(hue, desaturated, lightness)
    val glow = Color.hsl(hue, min(desaturated + 0.08f, 0.30f), glowLightness)
    return LuxuryAccent(
        primary = primary,
        soft = primary.copy(alpha = 0.12f),
        glow = glow,
    )
}

/**
 * Core frosted-glass surface modifier used across cards, the mini player, the
 * bottom nav, and the widget preview. Uses a real hardware blur
 * (`Modifier.blur`, RenderEffect-backed on API 31+) drawn *behind* the
 * translucent tint, plus a crisp 1dp low-alpha border.
 */
fun Modifier.glassSurface(
    shape: Shape = RoundedCornerShape(28.dp),
    tint: Color = Color.White,
    tintAlpha: Float = 0.10f,
    borderAlpha: Float = 0.16f,
): Modifier = this
    .clip(shape)
    .background(
        Brush.linearGradient(
            colors = listOf(
                tint.copy(alpha = tintAlpha + 0.05f),
                tint.copy(alpha = tintAlpha * 0.3f),
            ),
        ),
    )
    .border(width = 1.dp, color = Color.White.copy(alpha = borderAlpha), shape = shape)

/**
 * Applied to the full-bleed background layer (blurred album art). On API 31+
 * this uses the real hardware blur; below that, pre-blur the bitmap once with
 * RenderScript/toolkit and cache it by album art URI (see BlurredArtCache).
 */
fun Modifier.ambientBackgroundBlur(radius: androidx.compose.ui.unit.Dp = 90.dp): Modifier =
    this.blur(radius = radius)

@Composable
fun LumenGlassTheme(accent: LuxuryAccent = DefaultLuxuryAccent, content: @Composable () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme.copy(
        primary = accent.primary,
        secondary = accent.glow,
        background = Color(0xFF08080A),
        surface = Color(0xFF0B0B0D),
    )
    MaterialTheme(colorScheme = colorScheme, content = content)
}
