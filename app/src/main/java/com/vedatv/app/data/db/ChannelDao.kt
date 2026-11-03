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

    @Query("SELECT * FROM channels WHERE lastWatchedAt > 0 ORDER BY lastWatchedAt DESC LIMIT 20")
    fun observeRecentlyWatched(): Flow<List<ChannelEntity>>

    @Query("SELECT DISTINCT country FROM channels WHERE country != '' ORDER BY country")
    fun observeCountries(): Flow<List<String>>

    @Query("SELECT DISTINCT language FROM channels WHERE language != '' ORDER BY language")
    fun observeLanguages(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(channels: List<ChannelEntity>)

    @Query("UPDATE channels SET isFavorite = :favorite WHERE id = :channelId")
    suspend fun setFavorite(channelId: Long, favorite: Boolean)

    @Query("UPDATE channels SET lastWatchedAt = :timestamp WHERE id = :channelId")
    suspend fun markWatched(channelId: Long, timestamp: Long)
}
