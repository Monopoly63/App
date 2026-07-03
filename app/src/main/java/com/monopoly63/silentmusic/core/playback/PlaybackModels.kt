package com.monopoly63.silentmusic.core.playback

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.monopoly63.silentmusic.core.media.AudioItem

data class PlaybackUiState(
    val isPlaying: Boolean = false,
    val title: String? = null,
    val artist: String? = null,
    val artworkUri: Uri? = null,
    val durationMs: Long = 0,
    val positionMs: Long = 0
)

fun AudioItem.toMediaItem(): MediaItem = MediaItem.Builder()
    .setMediaId(mediaStoreId.toString())
    .setUri(contentUri)
    .setMediaMetadata(
        MediaMetadata.Builder().setTitle(title).setArtist(artist).setAlbumTitle(album).setArtworkUri(albumArtUri).build()
    ).build()
