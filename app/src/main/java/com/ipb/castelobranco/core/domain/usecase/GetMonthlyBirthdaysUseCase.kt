package com.ipb.castelobranco.core.domain.usecase

import com.ipb.castelobranco.core.domain.model.Birthday
import com.ipb.castelobranco.core.domain.repository.MembersRepository
import com.ipb.castelobranco.core.domain.snapshot.RefreshResult
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMonthlyBirthdaysUseCase @Inject constructor(
    private val repository: MembersRepository,
) {
    fun observe(): Flow<SnapshotState<List<Birthday>>> = repository.observeBirthdays()
    suspend fun refresh(): RefreshResult = repository.refreshBirthdays()
}
