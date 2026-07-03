package com.monopoly63.hablas.playback

import android.content.Context
import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import com.monopoly63.hablas.core.AudioTrack
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class HablasPlayer(private val context: Context) {
    private val appContext = context.applicationContext
    private val player: ExoPlayer = PlayerHolder.get(appContext)

    val state: Flow<PlayerState> = callbackFlow {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) { trySend(snapshot()) }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) { trySend(snapshot()) }
            override fun onPlaybackStateChanged(playbackState: Int) { trySend(snapshot()) }
        }
        val ticker = launch {
            while (true) {
                trySend(snapshot())
                delay(500)
            }
        }
        player.addListener(listener)
        trySend(snapshot())
        awaitClose {
            ticker.cancel()
            player.removeListener(listener)
        }
    }.distinctUntilChanged()

    fun playQueue(tracks: List<AudioTrack>, startIndex: Int) {
        ensureService()
        player.setMediaItems(tracks.map { it.toMediaItem() }, startIndex, 0L)
        player.prepare()
        player.play()
    }
    fun playPause() {
        ensureService()
        if (player.isPlaying) player.pause() else player.play()
    }
    fun next() { player.seekToNextMediaItem() }
    fun setShuffle(enabled: Boolean) { player.shuffleModeEnabled = enabled }
    fun setRepeatMode(mode: Int) { player.repeatMode = when (mode) { 1 -> Player.REPEAT_MODE_ALL; 2 -> Player.REPEAT_MODE_ONE; else -> Player.REPEAT_MODE_OFF } }
    fun previous() { player.seekToPreviousMediaItem() }
    fun seek(positionMs: Long) { player.seekTo(positionMs) }
    fun release() { /* Shared process player is owned by the MediaSessionService/process. */ }

    private fun ensureService() {
        val intent = Intent(appContext, PlaybackService::class.java)
        appContext.startService(intent)
    }

    private fun snapshot(): PlayerState {
        val md = player.currentMediaItem?.mediaMetadata
        val duration = player.duration.takeIf { it > 0 } ?: 0L
        return PlayerState(
            isPlaying = player.isPlaying,
            title = md?.title?.toString(),
            artist = md?.artist?.toString(),
            artworkUri = md?.artworkUri,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = duration
        )
    }
}

data class PlayerState(
    val isPlaying: Boolean = false,
    val title: String? = null,
    val artist: String? = null,
    val artworkUri: android.net.Uri? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L
)

private fun AudioTrack.toMediaItem(): MediaItem = MediaItem.Builder()
    .setMediaId(id.toString())
    .setUri(contentUri)
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setArtworkUri(albumArtUri)
            .build()
    )
    .build()
