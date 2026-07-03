package com.lumen.player.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/* ---------------------------------------------------------------------- */
/*  Entities                                                               */
/* ---------------------------------------------------------------------- */

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val mediaStoreId: Long,
    val addedAt: Long = System.currentTimeMillis(),
)

/**
 * The core of the "Folder Whitelist/Filter" feature. Every directory the
 * DirectoryScanner discovers gets a row here. `included = false` means the
 * folder (and everything beneath it) is hidden from every view — this is how
 * WhatsApp Voice Notes / Call Recordings / Ringtones get silently excluded.
 */
@Entity(tableName = "folder_filters")
data class FolderFilterEntity(
    @PrimaryKey val path: String, // absolute directory path, e.g. /storage/emulated/0/Music/Rock
    val included: Boolean = true,
    val displayName: String,
    val parentPath: String?,
    val isAutoSuspicious: Boolean = false, // heuristically flagged (WhatsApp/Voice Notes/etc.)
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "playlist_song_cross_ref",
    primaryKeys = ["playlistId", "mediaStoreId"],
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val mediaStoreId: Long,
    val position: Int,
)

/* ---------------------------------------------------------------------- */
/*  DAOs                                                                   */
/* ---------------------------------------------------------------------- */

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE mediaStoreId = :id)")
    fun isFavorite(id: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE mediaStoreId = :id")
    suspend fun remove(id: Long)

    @Transaction
    suspend fun toggle(id: Long) {
        if (isFavoriteOnce(id)) remove(id) else add(FavoriteEntity(id))
    }

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE mediaStoreId = :id)")
    suspend fun isFavoriteOnce(id: Long): Boolean
}

@Dao
interface FolderFilterDao {
    @Query("SELECT * FROM folder_filters ORDER BY path")
    fun observeAll(): Flow<List<FolderFilterEntity>>

    @Query("SELECT path FROM folder_filters WHERE included = 0")
    suspend fun excludedPaths(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entities: List<FolderFilterEntity>)

    @Query("UPDATE folder_filters SET included = :included WHERE path = :path")
    suspend fun setIncluded(path: String, included: Boolean)

    @Query("UPDATE folder_filters SET included = :included WHERE path LIKE :pathPrefix || '%'")
    suspend fun setIncludedRecursive(pathPrefix: String, included: Boolean)
}

@Dao
interface PlaylistDao {
    @Transaction
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Insert
    suspend fun createPlaylist(playlist: PlaylistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSong(ref: PlaylistSongCrossRef)

    @Query("SELECT mediaStoreId FROM playlist_song_cross_ref WHERE playlistId = :playlistId ORDER BY position")
    fun observeSongIds(playlistId: Long): Flow<List<Long>>
}

/* ---------------------------------------------------------------------- */
/*  Database                                                               */
/* ---------------------------------------------------------------------- */

@Database(
    entities = [
        FavoriteEntity::class,
        FolderFilterEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun folderFilterDao(): FolderFilterDao
    abstract fun playlistDao(): PlaylistDao
}
