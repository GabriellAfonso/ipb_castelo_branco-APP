package com.gabrielafonso.ipb.castelobranco.features.hymnal.data.repository

import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.model.Hymn
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.repository.HymnalRepository
import com.gabrielafonso.ipb.castelobranco.features.hymnal.data.snapshot.HymnalSnapshotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import javax.inject.Inject
class HymnalRepositoryImpl @Inject constructor(
    private val snapshot: HymnalSnapshotRepository
) : HymnalRepository {

    override fun observeHymnal(): Flow<List<Hymn>> =
        snapshot.observe()
            .map { it ?: emptyList() }

    override suspend fun refreshHymnal() = snapshot.refresh()
}
