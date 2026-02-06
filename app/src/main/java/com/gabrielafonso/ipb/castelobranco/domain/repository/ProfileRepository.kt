package com.gabrielafonso.ipb.castelobranco.domain.repository

interface ProfileRepository {
    suspend fun uploadProfilePhoto(bytes: ByteArray, fileName: String = "profile.jpg"): Result<String?>
    suspend fun deleteProfilePhoto(): Result<Unit>
}
