package com.vedatv.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vedatv.app.VedaTvApp
import com.vedatv.app.data.db.PlaylistEntity
import com.vedatv.app.data.repository.IptvRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PlaylistsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = IptvRepository(
        playlistDao = (application as VedaTvApp).database.playlistDao(),
        channelDao = application.database.channelDao(),
        epgDao = application.database.epgDao()
    )

    private val _playlists = MutableStateFlow<List<PlaylistEntity>>(emptyList())
    val playlists: StateFlow<List<PlaylistEntity>> = _playlists

    init {
        repository.observePlaylists()
            .onEach { _playlists.value = it }
            .launchIn(viewModelScope)
    }

    fun addPlaylist(name: String, url: String, epgUrl: String = "") {
        viewModelScope.launch {
            repository.addPlaylist(name, url)
                .onSuccess {
                    if (epgUrl.isNotBlank()) {
                        repository.refreshEpg(epgUrl)
                    }
                }
        }
    }

    fun importFromFile(name: String, content: String) {
        viewModelScope.launch {
            repository.importPlaylistFromFile(name, content)
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
