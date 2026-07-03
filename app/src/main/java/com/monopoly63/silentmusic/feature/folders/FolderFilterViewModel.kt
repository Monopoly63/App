package com.monopoly63.silentmusic.feature.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monopoly63.silentmusic.core.database.dao.FolderRulesDao
import com.monopoly63.silentmusic.core.database.entities.FolderRuleEntity
import com.monopoly63.silentmusic.core.database.entities.FolderRuleType
import com.monopoly63.silentmusic.core.media.FolderNode
import com.monopoly63.silentmusic.core.media.FolderTreeBuilder
import com.monopoly63.silentmusic.core.media.MediaStoreScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FolderFilterUiState(val folders: List<FolderNode> = emptyList())

@HiltViewModel
class FolderFilterViewModel @Inject constructor(scanner: MediaStoreScanner, private val dao: FolderRulesDao, builder: FolderTreeBuilder) : ViewModel() {
    val uiState: StateFlow<FolderFilterUiState> = combine(scanner.observeAudioLibrary(), dao.observeRules()) { songs, rules -> FolderFilterUiState(builder.build(songs, rules)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FolderFilterUiState())
    fun setRule(path: String, name: String, type: FolderRuleType) = viewModelScope.launch { dao.upsertRule(FolderRuleEntity(normalizedPath = path, displayName = name, ruleType = type)) }
    fun clear(path: String) = viewModelScope.launch { dao.deleteRule(path) }
}
