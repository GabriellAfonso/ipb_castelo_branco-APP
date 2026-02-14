package com.gabrielafonso.ipb.castelobranco.features.hymnal.data.repository

import com.gabrielafonso.ipb.castelobranco.core.data.repository.base.BaseListSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.core.data.local.JsonSnapshotStorage
import com.gabrielafonso.ipb.castelobranco.features.hymnal.data.api.HymnalApi
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.model.Hymn
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.model.HymnLyric
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.model.HymnLyricType
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.repository.HymnalRepository
import com.gabrielafonso.ipb.castelobranco.features.hymnal.data.dto.HymnDto
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject

class HymnalRepositoryImpl @Inject constructor(
    private val api: HymnalApi,
    private val jsonStorage: JsonSnapshotStorage
) : HymnalRepository {

    companion object {
        private const val KEY_HYMNAL = "hymnal"
        private const val TAG = "HymnalRepositoryImpl"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        encodeDefaults = true
    }

    private val base = object : BaseListSnapshotRepository<HymnDto, Hymn>(
        json = json,
        jsonStorage = jsonStorage,
        dtoListSerializer = ListSerializer(HymnDto.serializer()),
        key = KEY_HYMNAL,
        tag = "observeHymnal",
        fetchNetwork = { ifNoneMatch -> api.getHymnal(ifNoneMatch) }
    ) {
        private fun lyricTypeOf(raw: String): HymnLyricType =
            when (raw.trim().lowercase()) {
                "verse" -> HymnLyricType.VERSE
                "chorus" -> HymnLyricType.CHORUS
                else -> HymnLyricType.OTHER
            }

        override fun mapToDomain(dto: List<HymnDto>): List<Hymn> =
            dto.map { h ->
                Hymn(
                    number = h.number,
                    title = h.title,
                    lyrics = h.lyrics.map { l ->
                        HymnLyric(
                            type = lyricTypeOf(l.type),
                            text = l.text
                        )
                    }
                )
            }.sortedWith(compareBy({ it.number.toIntOrNull() ?: Int.MAX_VALUE }, { it.number }))
    }

    override fun observeHymnal() = base.observeSnapshotList()
    override suspend fun refreshHymnal() = base.refreshSnapshotList()
}