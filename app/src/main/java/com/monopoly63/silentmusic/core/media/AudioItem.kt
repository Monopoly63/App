package com.monopoly63.silentmusic.core.media

import android.net.Uri

data class AudioItem(
    val mediaStoreId: Long,
    val title: String,
    val artist: String?,
    val album: String?,
    val durationMs: Long,
    val sizeBytes: Long,
    val contentUri: Uri,
    val albumArtUri: Uri?,
    val relativePath: String?,
    val absolutePath: String?,
    val dateAdded: Long
)
