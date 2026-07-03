package com.monopoly63.silentmusic.feature.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monopoly63.silentmusic.core.playback.PlaybackUiState
import com.monopoly63.silentmusic.core.playback.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(private val repo: PlayerRepository) : ViewModel() {
    val state: StateFlow<PlaybackUiState> = repo.playbackState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlaybackUiState())
    fun playPause() = repo.playPause(); fun next() = repo.next(); fun previous() = repo.previous()
}
