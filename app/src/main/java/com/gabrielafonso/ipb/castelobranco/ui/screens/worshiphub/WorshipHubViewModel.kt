// app/src/main/java/com/gabrielafonso/ipb/castelobranco/ui/screens/worshiphub/WorshipHubViewModel.kt
package com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.data.repository.SongsRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.domain.model.SundaySet
import com.gabrielafonso.ipb.castelobranco.domain.model.TopSong
import com.gabrielafonso.ipb.castelobranco.domain.model.TopTone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorshipHubViewModel(
    private val repository: SongsRepositoryImpl
) : ViewModel() {

    private val _lastSundays = MutableStateFlow<List<SundaySet>>(emptyList())
    val lastSundays: StateFlow<List<SundaySet>> = _lastSundays.asStateFlow()

    private val _topSongs = MutableStateFlow<List<TopSong>>(emptyList())
    val topSongs: StateFlow<List<TopSong>> = _topSongs.asStateFlow()

    private val _topTones = MutableStateFlow<List<TopTone>>(emptyList())
    val topTones: StateFlow<List<TopTone>> = _topTones.asStateFlow()



    init {
        viewModelScope.launch {
            repository.observeSongsBySunday().collect { _lastSundays.value = it }
        }
        viewModelScope.launch {
            repository.observeTopSongs().collect { _topSongs.value = it }
        }
        viewModelScope.launch {
            repository.observeTopTones().collect { _topTones.value = it }
        }
    }
}
