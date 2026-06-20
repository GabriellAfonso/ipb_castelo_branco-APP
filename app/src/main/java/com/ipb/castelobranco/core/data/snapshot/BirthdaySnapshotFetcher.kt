package com.ipb.castelobranco.core.data.snapshot

import com.ipb.castelobranco.core.data.api.MembersApi
import com.ipb.castelobranco.core.data.dto.BirthdaysResponseDto
import com.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import java.time.LocalDate
import javax.inject.Inject

class BirthdaySnapshotFetcher @Inject constructor(
    private val api: MembersApi,
) : SnapshotFetcher<BirthdaysResponseDto>,
    RetrofitSnapshotFetcher<BirthdaysResponseDto>(
        call = { etag -> api.getBirthdays(month = LocalDate.now().monthValue, ifNoneMatch = etag) }
    )
