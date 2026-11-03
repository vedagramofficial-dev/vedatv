package com.vedatv.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vedatv.app.VedaTvApp
import com.vedatv.app.data.db.ChannelEntity
import com.vedatv.app.data.repository.IptvRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class GroupsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = IptvRepository(
        playlistDao = (application as VedaTvApp).database.playlistDao(),
        channelDao = application.database.channelDao(),
        epgDao = application.database.epgDao()
    )

    private val _channels = MutableStateFlow<List<ChannelEntity>>(emptyList())
    val channels: StateFlow<List<ChannelEntity>> = _channels

    fun load(playlistId: Long) {
        repository.observeChannels(playlistId)
            .onEach { _channels.value = it }
            .launchIn(viewModelScope)
    }
}
