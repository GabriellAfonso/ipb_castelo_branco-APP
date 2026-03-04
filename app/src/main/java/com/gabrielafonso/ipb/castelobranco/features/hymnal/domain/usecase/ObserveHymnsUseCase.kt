package com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.usecase

import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.model.Hymn
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.repository.HymnalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveHymnsUseCase @Inject constructor(
    private val repository: HymnalRepository,
) {
    operator fun invoke(): Flow<SnapshotState<List<Hymn>>> = repository.observeHymnal()
}
