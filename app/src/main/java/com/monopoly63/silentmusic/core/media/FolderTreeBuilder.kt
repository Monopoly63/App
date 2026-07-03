package com.monopoly63.silentmusic.core.media

import com.monopoly63.silentmusic.core.database.entities.FolderRuleEntity
import com.monopoly63.silentmusic.core.database.entities.FolderRuleType
import javax.inject.Inject

data class FolderNode(val name: String, val path: String, val children: List<FolderNode>, val songCount: Int, val isIncluded: Boolean, val isExcluded: Boolean)

class FolderTreeBuilder @Inject constructor() {
    fun build(songs: List<AudioItem>, rules: List<FolderRuleEntity>): List<FolderNode> {
        val counts = songs.mapNotNull { it.relativePath }.groupingBy { it }.eachCount()
        return counts.map { (path, count) ->
            FolderNode(
                name = path.trim('/').substringAfterLast('/').ifBlank { path },
                path = path,
                children = emptyList(),
                songCount = count,
                isIncluded = rules.any { it.ruleType == FolderRuleType.INCLUDE && it.normalizedPath.equals(path, true) },
                isExcluded = rules.any { it.ruleType == FolderRuleType.EXCLUDE && it.normalizedPath.equals(path, true) }
            )
        }.sortedBy { it.path.lowercase() }
    }
}
