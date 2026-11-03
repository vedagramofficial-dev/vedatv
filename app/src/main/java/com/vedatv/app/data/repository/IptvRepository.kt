package com.vedatv.app.data.repository

import com.vedatv.app.data.db.ChannelDao
import com.vedatv.app.data.db.ChannelEntity
import com.vedatv.app.data.db.EpgDao
import com.vedatv.app.data.db.EpgProgramEntity
import com.vedatv.app.data.db.PlaylistDao
import com.vedatv.app.data.db.PlaylistEntity
import com.vedatv.app.data.m3u.M3UParser
import com.vedatv.app.data.xmltv.XmlTvParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.net.URL

class IptvRepository(
    private val playlistDao: PlaylistDao,
    private val channelDao: ChannelDao,
    private val epgDao: EpgDao
) {

    fun observePlaylists(): Flow<List<PlaylistEntity>> = playlistDao.observePlaylists()

    fun observeChannels(playlistId: Long): Flow<List<ChannelEntity>> =
        channelDao.observeChannels(playlistId)

    fun observeFavorites(): Flow<List<ChannelEntity>> = channelDao.observeFavorites()

    fun observeRecentlyWatched(): Flow<List<ChannelEntity>> =
        channelDao.observeRecentlyWatched()

    fun observeCountries(): Flow<List<String>> = channelDao.observeCountries()

    fun observeLanguages(): Flow<List<String>> = channelDao.observeLanguages()

    fun observePrograms(channelTvgId: String): Flow<List<EpgProgramEntity>> =
        epgDao.observePrograms(channelTvgId, System.currentTimeMillis())

    fun observeCurrentProgram(channelTvgId: String): Flow<EpgProgramEntity?> =
        epgDao.observeCurrentProgram(channelTvgId, System.currentTimeMillis())

    suspend fun addPlaylist(name: String, url: String): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            val playlistId = playlistDao.insert(
                PlaylistEntity(name = name, url = url)
            )
            refreshPlaylist(playlistId, url)
            playlistId
        }
    }

    suspend fun importPlaylistFromFile(name: String, content: String): Result<Long> =
        withContext(Dispatchers.IO) {
            runCatching {
                val playlistId = playlistDao.insert(
                    PlaylistEntity(name = name, url = "local://$name")
                )
                val channels = M3UParser.parse(content, playlistId)
                channelDao.insertAll(channels)
                playlistId
            }
        }

    suspend fun refreshPlaylist(playlist: PlaylistEntity): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (playlist.url.startsWith("local://")) {
                return@runCatching
            }
            refreshPlaylist(playlist.id, playlist.url)
        }
    }

    suspend fun refreshEpg(xmlTvUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            URL(xmlTvUrl).openStream().use { stream ->
                val programs = XmlTvParser.parse(InputStreamReader(stream, Charsets.UTF_8))
                epgDao.clear()
                epgDao.insertAll(programs)
            }
        }
    }

    private suspend fun refreshPlaylist(playlistId: Long, url: String) {
        val oldChannels = channelDao.observeChannels(playlistId).first()
        val favoriteKeys = oldChannels
            .filter { it.isFavorite }
            .map { it.name to it.url }
            .toSet()

        val watchedAtByKey = oldChannels
            .filter { it.lastWatchedAt > 0 }
            .associate { (it.name to it.url) to it.lastWatchedAt }

        val text = URL(url).readText()
        val newChannels = M3UParser.parse(text, playlistId).map { channel ->
            val key = channel.name to channel.url
            channel.copy(
                isFavorite = key in favoriteKeys,
                lastWatchedAt = watchedAtByKey[key] ?: 0L
            )
        }

        playlistDao.deleteChannels(playlistId)
        channelDao.insertAll(newChannels)
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
