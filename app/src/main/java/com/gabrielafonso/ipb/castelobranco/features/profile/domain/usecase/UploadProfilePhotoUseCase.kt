package com.gabrielafonso.ipb.castelobranco.features.profile.domain.usecase

import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.RefreshResult
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UploadProfilePhotoUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    sealed interface Result {
        data object Success : Result
        data class Failure(val message: String) : Result
    }

    suspend operator fun invoke(bytes: ByteArray, fileName: String): Result {
        return try {
            repository.uploadProfilePhoto(bytes, fileName).getOrThrow()

            when (repository.refreshMeProfile()) {
                is RefreshResult.Error -> return Result.Failure("Falha ao atualizar perfil")
                else -> Unit
            }

            val profile = repository.observeMeProfile()
                .first { it is SnapshotState.Data }
                .let { (it as SnapshotState.Data).value }

            val url = profile.photoUrl?.trim()
            if (!url.isNullOrBlank()) {
                repository.downloadAndPersistProfilePhoto(url).getOrThrow()
            }

            Result.Success
        } catch (t: Throwable) {
            Result.Failure(t.message ?: "Falha ao enviar imagem")
        }
    }
}
