package com.vedatv.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long,
    val name: String,
    val group: String,
    val logo: String,
    val url: String,
    val isFavorite: Boolean = false
)
