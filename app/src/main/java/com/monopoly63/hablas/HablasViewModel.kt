package com.monopoly63.hablas

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monopoly63.hablas.core.AudioTrack
import com.monopoly63.hablas.core.MediaStoreScanner
import com.monopoly63.hablas.data.FavoriteEntity
import com.monopoly63.hablas.data.FolderRuleEntity
import com.monopoly63.hablas.data.HablasDatabase
import com.monopoly63.hablas.data.PlayHistoryEntity
import com.monopoly63.hablas.playback.HablasPlayer
import com.monopoly63.hablas.playback.PlayerState
import com.monopoly63.hablas.widget.HablasWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HablasViewModel(app: Application) : AndroidViewModel(app) {
    private val scanner = MediaStoreScanner(app)
    private val player = HablasPlayer(app)
    private val prefs = app.getSharedPreferences("hablas", Context.MODE_PRIVATE)
    private val db = HablasDatabase.get(app)

    private val _tracks = MutableStateFlow<List<AudioTrack>>(emptyList())
    val tracks: StateFlow<List<AudioTrack>> = _tracks

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _favorites = MutableStateFlow<Set<Long>>(emptySet())
    val favorites: StateFlow<Set<Long>> = _favorites

    private val _includedFolders = MutableStateFlow<Set<String>>(emptySet())
    val includedFolders: StateFlow<Set<String>> = _includedFolders

    private val _excludedFolders = MutableStateFlow<Set<String>>(emptySet())
    val excludedFolders: StateFlow<Set<String>> = _excludedFolders

    private val _playHistory = MutableStateFlow<List<PlayHistoryEntity>>(emptyList())
    val playHistory: StateFlow<List<PlayHistoryEntity>> = _playHistory

    private val _includeOnlyMode = MutableStateFlow(prefs.getBoolean("includeOnlyMode", false))
    val includeOnlyMode: StateFlow<Boolean> = _includeOnlyMode

    private val _shuffle = MutableStateFlow(false)
    val shuffle: StateFlow<Boolean> = _shuffle

    private val _repeatMode = MutableStateFlow(0) // 0 off, 1 all, 2 one
    val repeatMode: StateFlow<Int> = _repeatMode

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState

    init {
        viewModelScope.launch { player.state.collect { _playerState.value = it; HablasWidgetProvider.update(getApplication(), it) } }
        viewModelScope.launch { db.favoritesDao().observeIds().collect { _favorites.value = it.toSet() } }
        viewModelScope.launch { db.folderRulesDao().observeRules().collect { rules ->
            _includedFolders.value = rules.filter { it.included }.map { it.path }.toSet()
            _excludedFolders.value = rules.filter { it.excluded }.map { it.path }.toSet()
        } }
        viewModelScope.launch { db.playHistoryDao().observeAll().collect { _playHistory.value = it } }
    }

    fun scanLibrary() {
        viewModelScope.launch {
            _isScanning.value = true
            _tracks.value = withContext(Dispatchers.IO) { scanner.scan() }
            _isScanning.value = false
        }
    }

    fun visibleTracks(): List<AudioTrack> = _tracks.value.filter { track -> isTrackVisible(track) }

    fun isTrackVisible(track: AudioTrack): Boolean {
        val included = _includedFolders.value
        val excluded = _excludedFolders.value
        if (track.folderPath in excluded) return false
        return !_includeOnlyMode.value || included.isEmpty() || track.folderPath in included
    }

    fun play(track: AudioTrack) {
        val queue = visibleTracks()
        val index = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        player.setShuffle(_shuffle.value)
        player.setRepeatMode(_repeatMode.value)
        player.playQueue(queue, index)
        recordPlay(track.id)
    }
    fun playPause() = player.playPause()
    fun next() = player.next()
    fun previous() = player.previous()
    fun seek(positionMs: Long) = player.seek(positionMs)
    fun toggleShuffle() { _shuffle.value = !_shuffle.value; player.setShuffle(_shuffle.value) }
    fun cycleRepeat() { _repeatMode.value = (_repeatMode.value + 1) % 3; player.setRepeatMode(_repeatMode.value) }

    fun toggleFavorite(id: Long) {
        viewModelScope.launch {
            if (id in _favorites.value) db.favoritesDao().delete(id) else db.favoritesDao().insert(FavoriteEntity(id))
        }
    }

    fun toggleFolderExcluded(path: String) {
        viewModelScope.launch {
            val current = path in _excludedFolders.value
            db.folderRulesDao().upsert(FolderRuleEntity(path = path, included = path in _includedFolders.value, excluded = !current))
        }
    }

    fun toggleFolderIncluded(path: String) {
        viewModelScope.launch {
            val current = path in _includedFolders.value
            db.folderRulesDao().upsert(FolderRuleEntity(path = path, included = !current, excluded = path in _excludedFolders.value))
        }
    }

    fun setIncludeOnlyMode(enabled: Boolean) {
        _includeOnlyMode.value = enabled
        prefs.edit().putBoolean("includeOnlyMode", enabled).apply()
    }

    private fun recordPlay(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val old = db.playHistoryDao().get(id)
            db.playHistoryDao().upsert(PlayHistoryEntity(id, (old?.playCount ?: 0) + 1, System.currentTimeMillis()))
        }
    }

    override fun onCleared() { player.release() }
}
