package com.vedatv.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vedatv.app.VedaTvApp
import com.vedatv.app.data.db.ChannelEntity
import com.vedatv.app.data.db.PlaylistEntity
import com.vedatv.app.data.repository.IptvRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = IptvRepository(
        playlistDao = (application as VedaTvApp).database.playlistDao(),
        channelDao = application.database.channelDao(),
        epgDao = application.database.epgDao()
    )

    val playlists: StateFlow<List<PlaylistEntity>> = repository.observePlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favorites: StateFlow<List<ChannelEntity>> = repository.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentlyWatched: StateFlow<List<ChannelEntity>> = repository.observeRecentlyWatched()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun refresh(playlist: PlaylistEntity) {
        viewModelScope.launch {
            repository.refreshPlaylist(playlist)
        }
    }
}
