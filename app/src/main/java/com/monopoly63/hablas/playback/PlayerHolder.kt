package com.monopoly63.hablas.playback

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer

object PlayerHolder {
    @Volatile private var instance: ExoPlayer? = null

    fun get(context: Context): ExoPlayer {
        return instance ?: synchronized(this) {
            instance ?: ExoPlayer.Builder(context.applicationContext)
                .setHandleAudioBecomingNoisy(true)
                .build()
                .also { instance = it }
        }
    }

    fun release() {
        synchronized(this) {
            instance?.release()
            instance = null
        }
    }
}
