package com.gabrielafonso.ipb.castelobranco.domain.repository

import com.gabrielafonso.ipb.castelobranco.domain.model.Hymn
import kotlinx.coroutines.flow.Flow

interface HymnalRepository {
    fun observeHymnal(): Flow<List<Hymn>>
    suspend fun refreshHymnal(): Boolean
}
