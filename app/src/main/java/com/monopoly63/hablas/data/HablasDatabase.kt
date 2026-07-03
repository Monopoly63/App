package com.monopoly63.hablas.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favorites")
data class FavoriteEntity(@PrimaryKey val trackId: Long, val addedAt: Long = System.currentTimeMillis())

@Entity(tableName = "folder_rules")
data class FolderRuleEntity(@PrimaryKey val path: String, val included: Boolean = false, val excluded: Boolean = false)

@Entity(tableName = "play_history")
data class PlayHistoryEntity(@PrimaryKey val trackId: Long, val playCount: Int = 0, val lastPlayedAt: Long = 0L)

@Dao
interface FavoritesDao {
    @Query("SELECT trackId FROM favorites") fun observeIds(): Flow<List<Long>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entity: FavoriteEntity)
    @Query("DELETE FROM favorites WHERE trackId = :id") suspend fun delete(id: Long)
}

@Dao
interface FolderRulesDao {
    @Query("SELECT * FROM folder_rules") fun observeRules(): Flow<List<FolderRuleEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(entity: FolderRuleEntity)
    @Query("DELETE FROM folder_rules WHERE path = :path") suspend fun delete(path: String)
}

@Dao
interface PlayHistoryDao {
    @Query("SELECT * FROM play_history") fun observeAll(): Flow<List<PlayHistoryEntity>>
    @Query("SELECT * FROM play_history WHERE trackId = :id LIMIT 1") suspend fun get(id: Long): PlayHistoryEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(entity: PlayHistoryEntity)
}

@Database(entities = [FavoriteEntity::class, FolderRuleEntity::class, PlayHistoryEntity::class], version = 1, exportSchema = false)
abstract class HablasDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
    abstract fun folderRulesDao(): FolderRulesDao
    abstract fun playHistoryDao(): PlayHistoryDao

    companion object {
        @Volatile private var instance: HablasDatabase? = null
        fun get(context: Context): HablasDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, HablasDatabase::class.java, "hablas.db").build().also { instance = it }
        }
    }
}
