package com.vedatv.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId ORDER BY name")
    fun observeChannels(playlistId: Long): Flow<List<ChannelEntity>>

    @Query(
        "SELECT * FROM channels WHERE playlistId = :playlistId AND " +
            "(name LIKE '%' || :query || '%' OR `group` LIKE '%' || :query || '%') ORDER BY name"
    )
    fun searchChannels(playlistId: Long, query: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE isFavorite = 1 ORDER BY name")
    fun observeFavorites(): Flow<List<ChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(channels: List<ChannelEntity>)

    @Query("UPDATE channels SET isFavorite = :favorite WHERE id = :channelId")
    suspend fun setFavorite(channelId: Long, favorite: Boolean)
}
