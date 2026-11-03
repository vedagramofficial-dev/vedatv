package com.vedatv.app.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vedatv.app.data.db.ChannelEntity
import com.vedatv.app.data.repository.IptvRepository
import com.vedatv.app.ui.viewmodels.VedaTvAppProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChannelsViewModel : ViewModel() {

    private val repository = IptvRepository(
        playlistDao = VedaTvAppProvider.database.playlistDao(),
        channelDao = VedaTvAppProvider.database.channelDao()
    )

    private val _channels = mutableStateOf<List<ChannelEntity>>(emptyList())
    val channels: State<List<ChannelEntity>> = _channels

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
