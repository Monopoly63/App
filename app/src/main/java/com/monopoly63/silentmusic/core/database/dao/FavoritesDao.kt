package com.monopoly63.silentmusic.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.monopoly63.silentmusic.core.database.entities.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC") fun observeFavorites(): Flow<List<FavoriteEntity>>
    @Query("SELECT mediaStoreId FROM favorites") fun observeFavoriteIds(): Flow<List<Long>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun addFavorite(entity: FavoriteEntity)
    @Query("DELETE FROM favorites WHERE mediaStoreId = :mediaStoreId") suspend fun removeFavorite(mediaStoreId: Long)
}
