package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.fetcher

import com.gabrielafonso.ipb.castelobranco.core.data.snapshot.RetrofitSnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.api.SongsTableApi
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.SongsBySundayDto
import javax.inject.Inject

class SongsBySundaySnapshotFetcher @Inject constructor(
    api: SongsTableApi
) : SnapshotFetcher<List<SongsBySundayDto>>,
    RetrofitSnapshotFetcher<List<SongsBySundayDto>>(
        call = { etag -> api.getSongsBySunday(etag) }
    )