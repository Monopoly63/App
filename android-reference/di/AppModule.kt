package com.lumen.player.di

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.room.Room
import com.lumen.player.data.AppDatabase
import com.lumen.player.data.FavoriteDao
import com.lumen.player.data.FolderFilterDao
import com.lumen.player.data.PlaylistDao
import com.lumen.player.repository.MusicRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * For a personal-use app, every dependency is scoped to the process (Singleton).
 * Hilt is used purely for wiring convenience; a hand-rolled `ServiceLocator` object
 * would work identically if you'd rather avoid annotation processing.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "lumen.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideFolderFilterDao(db: AppDatabase): FolderFilterDao = db.folderFilterDao()

    @Provides
    fun providePlaylistDao(db: AppDatabase): PlaylistDao = db.playlistDao()

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer =
        ExoPlayer.Builder(context)
            .setHandleAudioBecomingNoisy(true) // pause on headphone unplug
            .build()

    @Provides
    @Singleton
    fun provideMusicRepository(
        @ApplicationContext context: Context,
        folderFilterDao: FolderFilterDao,
        favoriteDao: FavoriteDao,
    ): MusicRepository = MusicRepository(context, folderFilterDao, favoriteDao)
}
