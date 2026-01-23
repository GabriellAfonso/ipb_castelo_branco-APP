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
            val result = repository.getSongsBySunday()

            if (result.isNullOrEmpty()) {
                Log.w(TAG, "getSongsBySunday retornou null ou lista vazia")
            } else {
                Log.d(
                    TAG,
                    "getSongsBySunday sucesso: ${result.size} dias"
                )

                result.forEach { day ->
                    Log.d(
                        TAG,
                        "Data: ${day.date}, músicas: ${day.songs.size}"
                    )

                    day.songs.forEach { song ->
                        Log.d(
                            TAG,
                            " - ${song.position}. ${song.title} (${song.artist})"
                        )
                    }
                }
            }
        }
    }
}
