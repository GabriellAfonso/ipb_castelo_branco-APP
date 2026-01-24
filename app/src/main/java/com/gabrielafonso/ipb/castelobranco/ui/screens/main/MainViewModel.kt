package com.gabrielafonso.ipb.castelobranco.ui.screens.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.data.repository.SongsRepositoryImpl
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: SongsRepositoryImpl
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    init {
        preload()
    }

    private fun preload() {
        viewModelScope.launch {
            val ok = repository.refreshSongsBySunday()
            repository.refreshTopSongs()
            repository.refreshTopTones()
            if (!ok) {
                Log.w(TAG, "refreshSongsBySunday falhou")
            } else {
                Log.d(TAG, "refreshSongsBySunday sucesso")
            }
        }
    }
}
