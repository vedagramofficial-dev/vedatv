package com.vedatv.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Insert
    suspend fun insert(playlist: PlaylistEntity): Long

    @Delete
    suspend fun delete(playlist: PlaylistEntity)

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteChannels(playlistId: Long)
}
