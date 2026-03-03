package com.gabrielafonso.ipb.castelobranco.features.schedule.data.snapshot

import com.gabrielafonso.ipb.castelobranco.core.data.snapshot.RetrofitSnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.profile.data.api.ProfileApi
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.profile.data.dto.MeProfileDto
import com.gabrielafonso.ipb.castelobranco.features.schedule.data.api.ScheduleApi
import com.gabrielafonso.ipb.castelobranco.features.schedule.data.dto.MonthScheduleDto
import javax.inject.Inject

class ScheduleSnapshotFetcher @Inject constructor(
    api: ScheduleApi
) : SnapshotFetcher<MonthScheduleDto>,
    RetrofitSnapshotFetcher<MonthScheduleDto>(
        call = { etag -> api.getMonthSchedule(etag) }
    )
