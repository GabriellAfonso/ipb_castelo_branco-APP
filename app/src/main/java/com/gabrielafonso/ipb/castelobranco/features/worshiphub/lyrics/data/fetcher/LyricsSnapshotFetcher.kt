package com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.data.fetcher

import com.gabrielafonso.ipb.castelobranco.core.data.snapshot.RetrofitSnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.data.api.LyricsApi
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.data.dto.LyricsDto
import javax.inject.Inject

class LyricsSnapshotFetcher @Inject constructor(
    api: LyricsApi,
) : SnapshotFetcher<List<LyricsDto>>,
    RetrofitSnapshotFetcher<List<LyricsDto>>(
        call = { etag -> api.getLyrics(etag) }
    )
