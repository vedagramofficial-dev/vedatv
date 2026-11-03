package com.vedatv.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vedatv.app.VedaTvApp
import com.vedatv.app.data.db.PlaylistEntity
import com.vedatv.app.data.repository.IptvRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PlaylistsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = IptvRepository(
        playlistDao = (application as VedaTvApp).database.playlistDao(),
        channelDao = application.database.channelDao()
    )

    private val _playlists = androidx.compose.runtime.mutableStateOf<List<PlaylistEntity>>(emptyList())
    val playlists: androidx.compose.runtime.State<List<PlaylistEntity>> = _playlists

    init {
        repository.observePlaylists()
            .onEach { _playlists.value = it }
            .launchIn(viewModelScope)
    }

    fun addPlaylist(name: String, url: String) {
        viewModelScope.launch {
            repository.addPlaylist(name, url)
        }
    }

    fun refresh(playlist: PlaylistEntity) {
        viewModelScope.launch {
            repository.refreshPlaylist(playlist)
        }
    }

    fun delete(playlist: PlaylistEntity) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
        }
    }
}
