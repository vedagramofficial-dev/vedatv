package com.vedatv.app.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vedatv.app.VedaTvApp
import com.vedatv.app.data.db.PlaylistEntity
import com.vedatv.app.data.repository.IptvRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PlaylistsViewModel : ViewModel() {

    private val repository = IptvRepository(
        playlistDao = VedaTvAppProvider.database.playlistDao(),
        channelDao = VedaTvAppProvider.database.channelDao()
    )

    private val _playlists = mutableStateOf<List<PlaylistEntity>>(emptyList())
    val playlists: State<List<PlaylistEntity>> = _playlists

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

object VedaTvAppProvider {
    val database: com.vedatv.app.data.db.AppDatabase
        get() = VedaTvAppHolder.database
}

object VedaTvAppHolder {
    lateinit var database: com.vedatv.app.data.db.AppDatabase
}
