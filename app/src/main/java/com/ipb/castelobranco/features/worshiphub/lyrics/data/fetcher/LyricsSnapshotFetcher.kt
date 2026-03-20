package com.ipb.castelobranco.features.worshiphub.lyrics.data.fetcher

import com.ipb.castelobranco.core.data.snapshot.RetrofitSnapshotFetcher
import com.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.ipb.castelobranco.features.worshiphub.lyrics.data.api.LyricsApi
import com.ipb.castelobranco.features.worshiphub.lyrics.data.dto.LyricsDto
import javax.inject.Inject

class LyricsSnapshotFetcher @Inject constructor(
    api: LyricsApi,
) : SnapshotFetcher<List<LyricsDto>>,
    RetrofitSnapshotFetcher<List<LyricsDto>>(
        call = { etag -> api.getLyrics(etag) }
    )
