package com.gabrielafonso.ipb.castelobranco.features.hymnal.data.snapshot

import com.gabrielafonso.ipb.castelobranco.core.data.snapshot.RetrofitSnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.hymnal.data.api.HymnalApi
import com.gabrielafonso.ipb.castelobranco.features.hymnal.data.dto.HymnDto
import com.gabrielafonso.ipb.castelobranco.features.profile.data.api.ProfileApi
import com.gabrielafonso.ipb.castelobranco.features.profile.data.dto.MeProfileDto
import javax.inject.Inject

class HymnalSnapshotFetcher @Inject constructor(
    api: HymnalApi
) : SnapshotFetcher<List<HymnDto>>,
    RetrofitSnapshotFetcher<List<HymnDto>>(
        call = { etag -> api.getHymnal(etag) }
    )

