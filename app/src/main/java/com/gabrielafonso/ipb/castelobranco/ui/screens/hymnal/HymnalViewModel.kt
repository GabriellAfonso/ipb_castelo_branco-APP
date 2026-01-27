package com.gabrielafonso.ipb.castelobranco.ui.screens.hymnal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.data.repository.HymnalRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.domain.model.Hymn
import com.gabrielafonso.ipb.castelobranco.domain.repository.HymnalRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class HymnalViewModel(
    private val repository: HymnalRepository
) : ViewModel() {

    private val _hymnal = MutableStateFlow<List<Hymn>>(emptyList())
    val hymnal: StateFlow<List<Hymn>> = _hymnal.asStateFlow()


    init {
        viewModelScope.launch { repository.observeHymnal().collect { _hymnal.value = it } }
    }


}
