package com.monopoly63.silentmusic.core.di

import android.content.Context
import androidx.room.Room
import com.monopoly63.silentmusic.core.database.SilentMusicDatabase
import com.monopoly63.silentmusic.core.database.dao.FavoritesDao
import com.monopoly63.silentmusic.core.database.dao.FolderRulesDao
import com.monopoly63.silentmusic.core.database.dao.PlaylistsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton fun database(@ApplicationContext context: Context): SilentMusicDatabase =
        Room.databaseBuilder(context, SilentMusicDatabase::class.java, "silent_music.db").build()
    @Provides fun favoritesDao(db: SilentMusicDatabase): FavoritesDao = db.favoritesDao()
    @Provides fun folderRulesDao(db: SilentMusicDatabase): FolderRulesDao = db.folderRulesDao()
    @Provides fun playlistsDao(db: SilentMusicDatabase): PlaylistsDao = db.playlistsDao()
}
