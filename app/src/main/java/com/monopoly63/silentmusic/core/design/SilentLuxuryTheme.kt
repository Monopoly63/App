package com.monopoly63.silentmusic.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import android.net.Uri

@Immutable
data class GlassColors(
    val backgroundBase: Color = Color(0xFF08090D),
    val backgroundAccent: Color = Color(0xFF171A22),
    val surfaceGlass: Color = Color.White.copy(alpha = 0.08f),
    val borderSubtle: Color = Color.White.copy(alpha = 0.16f),
    val textPrimary: Color = Color(0xFFF5F3EF),
    val textSecondary: Color = Color(0xFFA9A7A2),
    val controlTint: Color = Color(0xFFEDE9DF)
)

val LocalGlassColors = staticCompositionLocalOf { GlassColors() }

@Composable
fun SilentLuxuryTheme(content: @Composable () -> Unit) {
    val colors = GlassColors()
    CompositionLocalProvider(LocalGlassColors provides colors) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                background = colors.backgroundBase,
                surface = colors.surfaceGlass,
                primary = colors.controlTint,
                onBackground = colors.textPrimary,
                onSurface = colors.textPrimary
            ),
            content = content
        )
    }
}

fun Modifier.glassSurface(
    shape: Shape = RoundedCornerShape(28.dp),
    backgroundAlpha: Float = 0.09f,
    borderAlpha: Float = 0.18f
): Modifier = clip(shape)
    .background(Color.White.copy(alpha = backgroundAlpha))
    .border(1.dp, Color.White.copy(alpha = borderAlpha), shape)

@Composable
fun AlbumArtBackdrop(
    artworkUri: Uri?,
    modifier: Modifier = Modifier,
    fallbackAccent: Color = Color(0xFF171A22),
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier.background(Color.Black)) {
        if (artworkUri != null) {
            AsyncImage(
                model = artworkUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize().scale(1.12f).blur(70.dp).alpha(0.72f)
            )
        } else {
            Box(Modifier.matchParentSize().background(Brush.radialGradient(listOf(fallbackAccent, Color(0xFF08090D), Color.Black), radius = 1200f)))
        }
        Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.48f)))
        Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.04f), Color.Transparent, Color.Black.copy(alpha = 0.55f)))))
        content()
    }
}
