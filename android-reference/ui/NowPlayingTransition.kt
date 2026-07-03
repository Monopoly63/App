package com.lumen.player.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lumen.player.data.model.Track

/**
 * The exact shared-element choreography behind "tap a song row → the album
 * art and mini-player morph into the full Now Playing screen" using
 * Compose's native SharedTransitionLayout (Compose 1.7+, stable in BOM
 * 2024.09+). This mirrors the layoutId-based crossfade used in the web
 * prototype (`framer-motion`'s `layoutId="player-art"` / `"player-surface"`).
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NowPlayingHost(tracks: List<Track>, currentTrack: Track?, onSelect: (Track) -> Unit) {
    var showFullPlayer by remember { mutableStateOf(false) }

    SharedTransitionLayout {
        if (!showFullPlayer) {
            SongListScreen(
                tracks = tracks,
                sharedTransitionScope = this@SharedTransitionLayout,
                onRowTap = { track ->
                    onSelect(track)
                    showFullPlayer = true
                },
            )
        } else {
            currentTrack?.let { track ->
                FullPlayerScreen(
                    track = track,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    onDismiss = { showFullPlayer = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SongListScreen(
    tracks: List<Track>,
    sharedTransitionScope: SharedTransitionScope,
    onRowTap: (Track) -> Unit,
) {
    with(sharedTransitionScope) {
        LazyColumn {
            items(tracks, key = { it.mediaStoreId }) { track ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    AsyncImage(
                        model = track.artworkUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(key = "art-${track.mediaStoreId}"),
                                animatedVisibilityScope = LocalAnimatedVisibilityScope.current!!,
                                boundsTransform = { _, _ -> tween(420) },
                            )
                            .clickableRow { onRowTap(track) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun FullPlayerScreen(
    track: Track,
    sharedTransitionScope: SharedTransitionScope,
    onDismiss: () -> Unit,
) {
    with(sharedTransitionScope) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = track.artworkUri,
                contentDescription = null,
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.Center)
                    .fillMaxWidth(0.78f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(key = "art-${track.mediaStoreId}"),
                        animatedVisibilityScope = LocalAnimatedVisibilityScope.current!!,
                        boundsTransform = { _, _ -> tween(420) },
                    ),
            )
        }
    }
}

/**
 * Placeholder — in the real app this comes from
 * `AnimatedContent`/`NavHost`'s `AnimatedVisibilityScope`, threaded down via
 * composition local exactly like `LocalAnimatedVisibilityScope` in Compose
 * Navigation's shared-element sample.
 */
private object LocalAnimatedVisibilityScope {
    val current: AnimatedVisibilityScope? = null
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier = this
