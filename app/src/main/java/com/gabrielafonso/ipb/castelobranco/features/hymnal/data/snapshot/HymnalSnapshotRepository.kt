package com.gabrielafonso.ipb.castelobranco.features.hymnal.data.snapshot

import com.gabrielafonso.ipb.castelobranco.core.data.local.JsonSnapshotStorage
import com.gabrielafonso.ipb.castelobranco.core.data.snapshot.BaseSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.features.hymnal.data.api.HymnalApi
import com.gabrielafonso.ipb.castelobranco.features.hymnal.data.dto.HymnDto
import com.gabrielafonso.ipb.castelobranco.features.hymnal.data.mapper.HymnMapper
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.model.Hymn
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject


class HymnalSnapshotRepository @Inject constructor(
    api: HymnalApi,
    jsonStorage: JsonSnapshotStorage,
    json: Json,
    private val mapper: HymnMapper
) : BaseSnapshotRepository<List<HymnDto>, List<Hymn>>(
    initialValue = emptyList(),
    json = json,
    jsonStorage = jsonStorage,
    serializer = ListSerializer(HymnDto.serializer()),
    key = "hymnal",
    tag = "Hymnal",
    fetchNetwork = api::getHymnal
) {
    override fun mapToDomain(dto: List<HymnDto>): List<Hymn> =
        mapper.map(dto)
}
