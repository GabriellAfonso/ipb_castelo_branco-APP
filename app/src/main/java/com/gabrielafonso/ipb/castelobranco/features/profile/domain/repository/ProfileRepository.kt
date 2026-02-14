package com.gabrielafonso.ipb.castelobranco.features.profile.domain.repository

import com.gabrielafonso.ipb.castelobranco.features.profile.data.dto.MeProfileDto
import java.io.File

interface ProfileRepository {
    suspend fun getMeProfile(): Result<MeProfileDto>
    suspend fun uploadProfilePhoto(bytes: ByteArray, fileName: String = "profile.jpg"): Result<String?>
    suspend fun deleteProfilePhoto(): Result<Unit>

    suspend fun downloadAndPersistProfilePhoto(photoUrl: String): Result<File?>
    suspend fun clearLocalProfilePhoto(): Result<Unit>


}