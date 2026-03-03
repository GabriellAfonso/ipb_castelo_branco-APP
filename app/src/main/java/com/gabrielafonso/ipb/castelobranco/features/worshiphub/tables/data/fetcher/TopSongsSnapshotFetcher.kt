package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.fetcher

import com.gabrielafonso.ipb.castelobranco.core.data.snapshot.RetrofitSnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.api.SongsTableApi
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.TopSongDto
import javax.inject.Inject

class TopSongsSnapshotFetcher @Inject constructor(
    api: SongsTableApi
) : SnapshotFetcher<List<TopSongDto>>,
    RetrofitSnapshotFetcher<List<TopSongDto>>(
        call = { etag -> api.getTopSongs(etag) }
    )