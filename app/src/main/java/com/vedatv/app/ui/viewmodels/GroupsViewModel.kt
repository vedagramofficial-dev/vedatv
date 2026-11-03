package com.vedatv.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vedatv.app.VedaTvApp
import com.vedatv.app.data.db.ChannelEntity
import com.vedatv.app.data.repository.IptvRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class GroupsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = IptvRepository(
        playlistDao = (application as VedaTvApp).database.playlistDao(),
        channelDao = application.database.channelDao(),
        epgDao = application.database.epgDao()
    )

    private val playlistId = androidx.compose.runtime.mutableStateOf(0L)

    val channels: StateFlow<List<ChannelEntity>> = repository.observeChannels(playlistId.value)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun load(id: Long) {
        playlistId.value = id
    }
}
