package com.monopoly63.silentmusic.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.monopoly63.silentmusic.core.database.converters.Converters
import com.monopoly63.silentmusic.core.database.dao.FavoritesDao
import com.monopoly63.silentmusic.core.database.dao.FolderRulesDao
import com.monopoly63.silentmusic.core.database.dao.PlaylistsDao
import com.monopoly63.silentmusic.core.database.entities.*

@Database(entities = [FavoriteEntity::class, FolderRuleEntity::class, PlaylistEntity::class, PlaylistSongEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class SilentMusicDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
    abstract fun folderRulesDao(): FolderRulesDao
    abstract fun playlistsDao(): PlaylistsDao
}
