package com.gabrielafonso.ipb.castelobranco.features.worshiphub.domain.repository

import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.Song
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.SuggestedSong
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.SundayPlayPushItem
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.SundaySet
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.TopSong
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.TopTone
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
    suspend fun refreshSuggestedSongs(fixedByPosition: Map<Int, Int>): Boolean

    fun observeAllSongs(): Flow<List<Song>>
    suspend fun refreshAllSongs(): Boolean

    // \- novo: push dos plays de domingo
    suspend fun pushSundayPlays(
        date: String,
        plays: List<SundayPlayPushItem>
    ): Boolean
}