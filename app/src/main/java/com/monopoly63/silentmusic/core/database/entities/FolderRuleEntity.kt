package com.monopoly63.silentmusic.core.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class FolderRuleType { INCLUDE, EXCLUDE }

@Entity(tableName = "folder_rules", indices = [Index(value = ["normalizedPath"], unique = true)])
data class FolderRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val normalizedPath: String,
    val displayName: String,
    val ruleType: FolderRuleType,
    val createdAt: Long = System.currentTimeMillis()
)
