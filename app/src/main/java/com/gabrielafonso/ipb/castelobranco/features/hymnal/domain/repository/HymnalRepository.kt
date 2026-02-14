package com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.repository

import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.model.Hymn
import kotlinx.coroutines.flow.Flow

interface HymnalRepository {
    fun observeHymnal(): Flow<List<Hymn>>
    suspend fun refreshHymnal(): Boolean
}