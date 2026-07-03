package com.monopoly63.silentmusic.feature.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monopoly63.silentmusic.core.database.dao.FavoritesDao
import com.monopoly63.silentmusic.core.database.entities.FavoriteEntity
import com.monopoly63.silentmusic.core.media.AudioItem
import com.monopoly63.silentmusic.core.media.MediaStoreScanner
import com.monopoly63.silentmusic.core.playback.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllSongsUiState(val songs: List<AudioItem> = emptyList(), val favoriteIds: Set<Long> = emptySet())

@HiltViewModel
class AllSongsViewModel @Inject constructor(
    scanner: MediaStoreScanner,
    private val favoritesDao: FavoritesDao,
    private val playerRepository: PlayerRepository
) : ViewModel() {
    val uiState: StateFlow<AllSongsUiState> = combine(scanner.observeAudioLibrary(), favoritesDao.observeFavoriteIds().map { it.toSet() }) { songs, favs ->
        AllSongsUiState(songs, favs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AllSongsUiState())

    fun playSong(song: AudioItem) { uiState.value.songs.indexOfFirst { it.mediaStoreId == song.mediaStoreId }.takeIf { it >= 0 }?.let { playerRepository.setQueue(uiState.value.songs, it) } }
    fun toggleFavorite(song: AudioItem) = viewModelScope.launch {
        if (uiState.value.favoriteIds.contains(song.mediaStoreId)) favoritesDao.removeFavorite(song.mediaStoreId)
        else favoritesDao.addFavorite(FavoriteEntity(mediaStoreId = song.mediaStoreId, title = song.title, artist = song.artist, album = song.album, durationMs = song.durationMs, contentUri = song.contentUri.toString(), albumArtUri = song.albumArtUri?.toString()))
    }
}
