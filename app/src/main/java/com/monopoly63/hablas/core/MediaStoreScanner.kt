package com.monopoly63.hablas.core

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore

class MediaStoreScanner(private val context: Context) {
    fun scan(): List<AudioTrack> {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.IS_MUSIC
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) projection += MediaStore.Audio.Media.RELATIVE_PATH
        else @Suppress("DEPRECATION") projection += MediaStore.Audio.Media.DATA

        val tracks = mutableListOf<AudioTrack>()
        context.contentResolver.query(
            collection,
            projection.toTypedArray(),
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > ?",
            arrayOf("30000"),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                "${MediaStore.Audio.Media.RELATIVE_PATH} COLLATE NOCASE ASC, ${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
            } else {
                "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
            }
        )?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val displayCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val pathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) c.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH) else @Suppress("DEPRECATION") c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val albumId = c.getLong(albumIdCol)
                val rawPath = clean(c.getString(pathCol), "Device Music/").replace('\\', '/')
                val display = clean(c.getString(displayCol), "Track")
                val folder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    rawPath.trim('/').ifBlank { "Device Music" }
                } else {
                    rawPath.substringBeforeLast('/', "Device Music").trim('/').ifBlank { "Device Music" }
                }
                tracks += AudioTrack(
                    id = id,
                    title = clean(c.getString(titleCol), display.substringBeforeLast('.')),
                    artist = clean(c.getString(artistCol), "Unknown Artist"),
                    album = clean(c.getString(albumCol), "Unknown Album"),
                    durationMs = c.getLong(durationCol),
                    contentUri = ContentUris.withAppendedId(collection, id),
                    albumArtUri = ContentUris.withAppendedId(ART_URI, albumId),
                    folderPath = folder,
                    fileName = display
                )
            }
        }
        return tracks.sortedWith(compareBy<AudioTrack, String>(String.CASE_INSENSITIVE_ORDER) { it.folderPath }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.title })
    }

    private fun clean(value: String?, fallback: String): String {
        val trimmed = value?.trim().orEmpty()
        return if (trimmed.isBlank() || trimmed.equals("<unknown>", true)) fallback else trimmed
    }

    companion object {
        private val ART_URI = android.net.Uri.parse("content://media/external/audio/albumart")
    }
}
