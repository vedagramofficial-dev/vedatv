package com.vedatv.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "epg_programs")
data class EpgProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelId: String,
    val title: String,
    val description: String,
    val startTime: Long,
    val endTime: Long
)
