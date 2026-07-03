package com.monopoly63.silentmusic.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.monopoly63.silentmusic.core.database.entities.FolderRuleEntity
import com.monopoly63.silentmusic.core.database.entities.FolderRuleType
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderRulesDao {
    @Query("SELECT * FROM folder_rules ORDER BY displayName ASC") fun observeRules(): Flow<List<FolderRuleEntity>>
    @Query("SELECT * FROM folder_rules WHERE ruleType = :type") fun observeRulesByType(type: FolderRuleType): Flow<List<FolderRuleEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertRule(rule: FolderRuleEntity)
    @Query("DELETE FROM folder_rules WHERE normalizedPath = :path") suspend fun deleteRule(path: String)
    @Query("DELETE FROM folder_rules") suspend fun clearRules()
}
