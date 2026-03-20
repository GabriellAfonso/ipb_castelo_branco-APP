package com.ipb.castelobranco.features.worshiphub.chordcharts.data.fetcher

import com.ipb.castelobranco.core.data.snapshot.RetrofitSnapshotFetcher
import com.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.ipb.castelobranco.features.worshiphub.chordcharts.data.api.ChordChartsApi
import com.ipb.castelobranco.features.worshiphub.chordcharts.data.dto.ChordChartDto
import javax.inject.Inject

class ChordChartsSnapshotFetcher @Inject constructor(
    api: ChordChartsApi,
) : SnapshotFetcher<List<ChordChartDto>>,
    RetrofitSnapshotFetcher<List<ChordChartDto>>(
        call = { etag -> api.getChordCharts(etag) }
    )
