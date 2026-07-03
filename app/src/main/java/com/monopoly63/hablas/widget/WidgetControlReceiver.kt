package com.monopoly63.hablas.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.monopoly63.hablas.playback.PlayerHolder

class WidgetControlReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val player = PlayerHolder.get(context)
        when (intent.action) {
            ACTION_WIDGET_PLAY_PAUSE -> if (player.isPlaying) player.pause() else player.play()
            ACTION_WIDGET_NEXT -> player.seekToNextMediaItem()
            ACTION_WIDGET_PREVIOUS -> player.seekToPreviousMediaItem()
        }
    }
}
