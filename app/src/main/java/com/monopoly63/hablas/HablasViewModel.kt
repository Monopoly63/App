package com.monopoly63.hablas

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monopoly63.hablas.core.AudioTrack
import com.monopoly63.hablas.core.MediaStoreScanner
import com.monopoly63.hablas.playback.HablasPlayer
import com.monopoly63.hablas.playback.PlayerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HablasViewModel(app: Application) : AndroidViewModel(app) {
    private val scanner = MediaStoreScanner(app)
    private val player = HablasPlayer(app)
    private val prefs = app.getSharedPreferences("hablas", Context.MODE_PRIVATE)

    private val _tracks = MutableStateFlow<List<AudioTrack>>(emptyList())
    val tracks: StateFlow<List<AudioTrack>> = _tracks

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _favorites = MutableStateFlow(loadFavorites())
    val favorites: StateFlow<Set<Long>> = _favorites

    private val _excludedFolders = MutableStateFlow(loadExcludedFolders())
    val excludedFolders: StateFlow<Set<String>> = _excludedFolders

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState

    init {
        viewModelScope.launch { player.state.collect { _playerState.value = it } }
    }

    fun scanLibrary() {
        viewModelScope.launch {
            _isScanning.value = true
            _tracks.value = withContext(Dispatchers.IO) { scanner.scan() }
            _isScanning.value = false
        }
    }

    fun visibleTracks(): List<AudioTrack> = _tracks.value.filter { it.folderPath !in _excludedFolders.value }

    fun play(track: AudioTrack) {
        val queue = visibleTracks()
        val index = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        player.playQueue(queue, index)
    }
    fun playPause() = player.playPause()
    fun next() = player.next()
    fun previous() = player.previous()
    fun seek(positionMs: Long) = player.seek(positionMs)

    fun toggleFavorite(id: Long) {
        val next = _favorites.value.toMutableSet().apply { if (!add(id)) remove(id) }
        _favorites.value = next
        prefs.edit().putStringSet("favorites", next.map { it.toString() }.toSet()).apply()
    }

    fun toggleFolder(path: String) {
        val next = _excludedFolders.value.toMutableSet().apply { if (!add(path)) remove(path) }
        _excludedFolders.value = next
        prefs.edit().putStringSet("excludedFolders", next).apply()
    }

    private fun loadFavorites(): Set<Long> = prefs.getStringSet("favorites", emptySet()).orEmpty().mapNotNull { it.toLongOrNull() }.toSet()
    private fun loadExcludedFolders(): Set<String> = prefs.getStringSet("excludedFolders", emptySet()).orEmpty()

    override fun onCleared() { player.release() }
}
