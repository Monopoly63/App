package com.monopoly63.silentmusic.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_songs", primaryKeys = ["playlistId", "mediaStoreId"])
data class PlaylistSongEntity(
    val playlistId: Long,
    val mediaStoreId: Long,
    val sortIndex: Int,
    val addedAt: Long = System.currentTimeMillis()
)
