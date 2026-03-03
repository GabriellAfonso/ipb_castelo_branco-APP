package com.gabrielafonso.ipb.castelobranco.features.profile.data.snapshot

import com.gabrielafonso.ipb.castelobranco.core.data.snapshot.RetrofitSnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.profile.data.api.ProfileApi
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.profile.data.dto.MeProfileDto
import javax.inject.Inject

class ProfileSnapshotFetcher @Inject constructor(
    api: ProfileApi
) : SnapshotFetcher<MeProfileDto>,
    RetrofitSnapshotFetcher<MeProfileDto>(
        call = { etag -> api.getMeProfile(etag) }
    )
