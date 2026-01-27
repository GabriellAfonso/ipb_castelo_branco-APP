// app/src/main/java/com/gabrielafonso/ipb/castelobranco/domain/repository/SongsRepository.kt
package com.gabrielafonso.ipb.castelobranco.domain.repository

import com.gabrielafonso.ipb.castelobranco.domain.model.SuggestedSong
import com.gabrielafonso.ipb.castelobranco.domain.model.SundaySet
import com.gabrielafonso.ipb.castelobranco.domain.model.TopSong
import com.gabrielafonso.ipb.castelobranco.domain.model.TopTone
import kotlinx.coroutines.flow.Flow

interface SongsRepository {
    fun observeSongsBySunday(): Flow<List<SundaySet>>
    suspend fun refreshSongsBySunday(): Boolean

    fun observeTopSongs(): Flow<List<TopSong>>
    suspend fun refreshTopSongs(): Boolean

    fun observeTopTones(): Flow<List<TopTone>>
    suspend fun refreshTopTones(): Boolean

    fun observeSuggestedSongs(): Flow<List<SuggestedSong>>
    suspend fun refreshSuggestedSongs(): Boolean
}
