package com.gabrielafonso.ipb.castelobranco.features.profile.domain.usecase

import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.RefreshResult
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.MeProfile
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class FetchProfileUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    fun observe(): Flow<SnapshotState<MeProfile>> = repository.observeMeProfile()

    suspend fun refresh(): RefreshResult = repository.refreshMeProfile()

    suspend fun downloadAndPersistPhoto(url: String): Result<File?> =
        repository.downloadAndPersistProfilePhoto(url)
}
