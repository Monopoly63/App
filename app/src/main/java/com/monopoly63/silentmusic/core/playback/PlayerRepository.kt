package com.monopoly63.silentmusic.core.playback

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.monopoly63.silentmusic.core.media.AudioItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepository @Inject constructor(private val player: ExoPlayer) {
    val playbackState: Flow<PlaybackUiState> = callbackFlow {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) { trySend(buildState()) }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) { trySend(buildState()) }
            override fun onPlaybackStateChanged(playbackState: Int) { trySend(buildState()) }
        }
        player.addListener(listener)
        trySend(buildState())
        awaitClose { player.removeListener(listener) }
    }.distinctUntilChanged()

    fun setQueue(songs: List<AudioItem>, startIndex: Int) {
        player.setMediaItems(songs.map { it.toMediaItem() }, startIndex, 0)
        player.prepare(); player.play()
    }
    fun playPause() { if (player.isPlaying) player.pause() else player.play() }
    fun next() = player.seekToNextMediaItem()
    fun previous() = player.seekToPreviousMediaItem()
    private fun buildState(): PlaybackUiState = player.currentMediaItem?.mediaMetadata.let { md ->
        PlaybackUiState(player.isPlaying, md?.title?.toString(), md?.artist?.toString(), md?.artworkUri, player.duration.coerceAtLeast(0), player.currentPosition.coerceAtLeast(0))
    }
}
