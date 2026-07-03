package com.monopoly63.silentmusic.core.media

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.monopoly63.silentmusic.core.database.dao.FolderRulesDao
import com.monopoly63.silentmusic.core.database.entities.FolderRuleEntity
import com.monopoly63.silentmusic.core.database.entities.FolderRuleType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val folderRulesDao: FolderRulesDao
) {
    fun observeAudioLibrary(): Flow<List<AudioItem>> = folderRulesDao.observeRules().mapLatest { scan(it) }.flowOn(Dispatchers.IO)

    private fun scan(rules: List<FolderRuleEntity>): List<AudioItem> {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = mutableListOf(
            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED, MediaStore.Audio.Media.IS_MUSIC
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) projection += MediaStore.Audio.Media.RELATIVE_PATH
        else @Suppress("DEPRECATION") projection += MediaStore.Audio.Media.DATA
        val result = mutableListOf<AudioItem>()
        context.contentResolver.query(collection, projection.toTypedArray(),
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > ?", arrayOf("30000"),
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC")?.use { c ->
            val id = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val title = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artist = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val album = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val duration = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val size = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val date = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val rel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) c.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH) else -1
            val abs = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) @Suppress("DEPRECATION") c.getColumnIndex(MediaStore.Audio.Media.DATA) else -1
            while (c.moveToNext()) {
                val mediaId = c.getLong(id)
                val relative = if (rel >= 0) c.getString(rel) else null
                val absolute = if (abs >= 0) c.getString(abs) else null
                val normalized = normalize(relative ?: absolute)
                if (!passesRules(normalized, rules)) continue
                result += AudioItem(
                    mediaStoreId = mediaId,
                    title = c.getString(title).orEmpty().ifBlank { "Unknown Title" },
                    artist = c.getString(artist), album = c.getString(album), durationMs = c.getLong(duration),
                    sizeBytes = c.getLong(size), contentUri = ContentUris.withAppendedId(collection, mediaId),
                    albumArtUri = ContentUris.withAppendedId(android.net.Uri.parse("content://media/external/audio/albumart"), mediaId),
                    relativePath = relative, absolutePath = absolute, dateAdded = c.getLong(date)
                )
            }
        }
        return result
    }

    private fun normalize(path: String?) = path?.replace("\\", "/")?.trim()?.removePrefix("/") ?: ""
    private fun passesRules(path: String, rules: List<FolderRuleEntity>): Boolean {
        val includes = rules.filter { it.ruleType == FolderRuleType.INCLUDE }
        val excludes = rules.filter { it.ruleType == FolderRuleType.EXCLUDE }
        if (excludes.any { path.startsWith(it.normalizedPath, ignoreCase = true) }) return false
        return includes.isEmpty() || includes.any { path.startsWith(it.normalizedPath, ignoreCase = true) }
    }
}
