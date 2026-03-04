package com.gabrielafonso.ipb.castelobranco.features.auth.domain.usecase

import com.gabrielafonso.ipb.castelobranco.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() = repository.signOut()
}
