package com.monopoly63.hablas.playback

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.monopoly63.hablas.core.AudioTrack
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class HablasPlayer(context: Context) {
    private val player = ExoPlayer.Builder(context).setHandleAudioBecomingNoisy(true).build()

    val state: Flow<PlayerState> = callbackFlow {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) { trySend(snapshot()) }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) { trySend(snapshot()) }
            override fun onPlaybackStateChanged(playbackState: Int) { trySend(snapshot()) }
        }
        player.addListener(listener)
        trySend(snapshot())
        awaitClose { player.removeListener(listener) }
    }.distinctUntilChanged()

    fun playQueue(tracks: List<AudioTrack>, startIndex: Int) {
        player.setMediaItems(tracks.map { it.toMediaItem() }, startIndex, 0L)
        player.prepare()
        player.play()
    }
    fun playPause() { if (player.isPlaying) player.pause() else player.play() }
    fun next() { player.seekToNextMediaItem() }
    fun previous() { player.seekToPreviousMediaItem() }
    fun release() { player.release() }

    private fun snapshot(): PlayerState {
        val md = player.currentMediaItem?.mediaMetadata
        return PlayerState(
            isPlaying = player.isPlaying,
            title = md?.title?.toString(),
            artist = md?.artist?.toString(),
            artworkUri = md?.artworkUri
        )
    }
}

data class PlayerState(
    val isPlaying: Boolean = false,
    val title: String? = null,
    val artist: String? = null,
    val artworkUri: android.net.Uri? = null
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
