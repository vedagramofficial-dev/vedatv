package com.vedatv.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vedatv.app.VedaTvApp
import com.vedatv.app.data.db.ChannelEntity
import com.vedatv.app.data.repository.IptvRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChannelsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = IptvRepository(
        playlistDao = (application as VedaTvApp).database.playlistDao(),
        channelDao = application.database.channelDao(),
        epgDao = application.database.epgDao()
    )

    private val _channels = androidx.compose.runtime.mutableStateOf<List<ChannelEntity>>(emptyList())
    val channels: androidx.compose.runtime.State<List<ChannelEntity>> = _channels

    private var currentJob: Job? = null

    fun load(playlistId: Long) {
        currentJob?.cancel()
        currentJob = repository.observeChannels(playlistId)
            .onEach { _channels.value = it }
            .launchIn(viewModelScope)
    }

    fun search(playlistId: Long, query: String) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            delay(250)
            repository.searchChannels(playlistId, query)
                .onEach { _channels.value = it }
                .launchIn(this)
        }
    }

    fun toggleFavorite(channel: ChannelEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(channel)
        }
    }
}
