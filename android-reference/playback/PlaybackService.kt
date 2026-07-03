package com.lumen.player.playback

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ControllerInfo
import com.lumen.player.MainActivity
import com.lumen.player.repository.MusicRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Background playback lives entirely here. The Compose UI *and* the Jetpack
 * Glance widget both bind a `MediaController` to this service — neither ever
 * touches ExoPlayer directly, and the widget's Play/Skip actions therefore
 * work even if the Activity/process hosting the UI has been killed.
 */
@UnstableApi
@AndroidEntryPoint
class PlaybackService : MediaLibraryService() {

    @Inject lateinit var exoPlayer: ExoPlayer
    @Inject lateinit var musicRepository: MusicRepository

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        val sessionActivityIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setSessionActivity(sessionActivityIntent)
            .setCallback(LibrarySessionCallback())
            .build()

        exoPlayer.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // Notify the Glance widget to refresh its glanceAppWidgetState.
                serviceScope.launch { WidgetUpdater.refresh(applicationContext) }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                serviceScope.launch { WidgetUpdater.refresh(applicationContext) }
            }
        })
    }

    override fun onGetSession(controllerInfo: ControllerInfo): MediaSession? = mediaSession

    /**
     * Builds the browsable tree (used by Android Auto / the widget's "queue"
     * concept) strictly from tracks that survive the folder whitelist.
     */
    private inner class LibrarySessionCallback : MediaLibrarySession.Callback {
        // onGetLibraryRoot / onGetChildren would query musicRepository.filteredTracks()
        // and map each Track to a MediaItem with matching MediaMetadata (art uri,
        // title, artist) — omitted here for brevity, see MusicRepository.kt.
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}

/** Thin facade the widget's action callbacks use to talk to the running session. */
object WidgetUpdater {
    suspend fun refresh(context: android.content.Context) {
        // GlanceAppWidgetManager(context).getGlanceIds(MusicGlanceWidget::class.java)
        //     .forEach { MusicGlanceWidget().update(context, it) }
    }
}
