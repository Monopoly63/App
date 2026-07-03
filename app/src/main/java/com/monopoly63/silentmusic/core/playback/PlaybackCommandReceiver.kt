package com.monopoly63.silentmusic.core.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

const val ACTION_PLAY_PAUSE = "com.monopoly63.silentmusic.action.PLAY_PAUSE"
const val ACTION_NEXT = "com.monopoly63.silentmusic.action.NEXT"
const val ACTION_PREVIOUS = "com.monopoly63.silentmusic.action.PREVIOUS"

@AndroidEntryPoint
class PlaybackCommandReceiver : BroadcastReceiver() {
    @Inject lateinit var repo: PlayerRepository
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PLAY_PAUSE -> repo.playPause()
            ACTION_NEXT -> repo.next()
            ACTION_PREVIOUS -> repo.previous()
        }
    }
}
