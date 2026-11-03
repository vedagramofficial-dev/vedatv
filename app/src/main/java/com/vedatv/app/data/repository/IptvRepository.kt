package com.vedatv.app.data.repository

import com.vedatv.app.data.db.ChannelDao
import com.vedatv.app.data.db.ChannelEntity
import com.vedatv.app.data.db.PlaylistDao
import com.vedatv.app.data.db.PlaylistEntity
import com.vedatv.app.data.m3u.M3UParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.net.URL

class IptvRepository(
    private val playlistDao: PlaylistDao,
    private val channelDao: ChannelDao
) {

    fun observePlaylists(): Flow<List<PlaylistEntity>> = playlistDao.observePlaylists()

    fun observeChannels(playlistId: Long): Flow<List<ChannelEntity>> =
        channelDao.observeChannels(playlistId)

    fun observeFavorites(): Flow<List<ChannelEntity>> = channelDao.observeFavorites()

    fun observeRecentlyWatched(): Flow<List<ChannelEntity>> =
        channelDao.observeRecentlyWatched()

    fun observeCountries(): Flow<List<String>> = channelDao.observeCountries()

    fun observeLanguages(): Flow<List<String>> = channelDao.observeLanguages()

    suspend fun addPlaylist(name: String, url: String): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            val playlistId = playlistDao.insert(
                PlaylistEntity(name = name, url = url)
            )
            refreshPlaylist(playlistId, url)
            playlistId
        }
    }

    suspend fun refreshPlaylist(playlist: PlaylistEntity): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            refreshPlaylist(playlist.id, playlist.url)
        }
    }

    private suspend fun refreshPlaylist(playlistId: Long, url: String) {
        val text = URL(url).readText()
        val channels = M3UParser.parse(text, playlistId)
        playlistDao.deleteChannels(playlistId)
        channelDao.insertAll(channels)
    }

    suspend fun deletePlaylist(playlist: PlaylistEntity) {
        playlistDao.deleteChannels(playlist.id)
        playlistDao.delete(playlist)
    }

    suspend fun toggleFavorite(channel: ChannelEntity) {
        channelDao.setFavorite(channel.id, !channel.isFavorite)
    }

    suspend fun markWatched(channel: ChannelEntity) {
        channelDao.markWatched(channel.id, System.currentTimeMillis())
    }
}
