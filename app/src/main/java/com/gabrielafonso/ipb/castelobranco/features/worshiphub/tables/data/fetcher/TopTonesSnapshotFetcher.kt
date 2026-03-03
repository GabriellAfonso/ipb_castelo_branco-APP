package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.fetcher

import com.gabrielafonso.ipb.castelobranco.core.data.snapshot.RetrofitSnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.api.SongsTableApi
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.TopToneDto
import javax.inject.Inject

class TopTonesSnapshotFetcher @Inject constructor(
    api: SongsTableApi
) : SnapshotFetcher<List<TopToneDto>>,
    RetrofitSnapshotFetcher<List<TopToneDto>>(
        call = { etag -> api.getTopTones(etag) }
    )