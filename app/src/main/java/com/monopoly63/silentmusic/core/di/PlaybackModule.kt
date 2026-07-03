package com.monopoly63.silentmusic.core.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlaybackModule {
    @Provides @Singleton fun exoPlayer(@ApplicationContext context: Context): ExoPlayer =
        ExoPlayer.Builder(context).setHandleAudioBecomingNoisy(true).build()
}
