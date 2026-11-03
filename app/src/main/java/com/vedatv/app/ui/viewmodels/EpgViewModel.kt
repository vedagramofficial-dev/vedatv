package com.vedatv.app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vedatv.app.VedaTvApp
import com.vedatv.app.data.db.EpgProgramEntity
import com.vedatv.app.data.repository.IptvRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EpgViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = IptvRepository(
        playlistDao = (application as VedaTvApp).database.playlistDao(),
        channelDao = application.database.channelDao(),
        epgDao = application.database.epgDao()
    )

    private val _programs = MutableStateFlow<List<EpgProgramEntity>>(emptyList())
    val programs: StateFlow<List<EpgProgramEntity>> = _programs

    private val _currentProgram = MutableStateFlow<EpgProgramEntity?>(null)
    val currentProgram: StateFlow<EpgProgramEntity?> = _currentProgram

    fun load(channelTvgId: String) {
        if (channelTvgId.isBlank()) return

        viewModelScope.launch {
            repository.observeCurrentProgram(channelTvgId)
                .collect { _currentProgram.value = it }
        }

        viewModelScope.launch {
            repository.observePrograms(channelTvgId)
                .collect { _programs.value = it }
        }
    }

    fun refreshEpg(xmlTvUrl: String) {
        viewModelScope.launch {
            repository.refreshEpg(xmlTvUrl)
        }
    }
}
