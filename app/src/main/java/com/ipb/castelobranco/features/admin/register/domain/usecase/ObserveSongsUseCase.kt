package com.ipb.castelobranco.features.admin.register.domain.usecase

import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.features.worshiphub.tables.domain.model.Song
import com.ipb.castelobranco.features.worshiphub.tables.domain.repository.SongsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSongsUseCase @Inject constructor(
    private val repository: SongsRepository
) {
    fun observe(): Flow<SnapshotState<List<Song>>> = repository.observeAllSongs()

    suspend fun refresh() {
        repository.refreshAllSongs()
    }
}
