package com.gabrielafonso.ipb.castelobranco.features.worshiphub.data.repository

import com.gabrielafonso.ipb.castelobranco.core.data.repository.base.BaseListSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.data.dto.AllSongDto
import com.gabrielafonso.ipb.castelobranco.data.api.BackendApi
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.data.dto.RegisterSundayPlayItemDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.data.dto.RegisterSundayPlaysRequestDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.data.dto.SongsBySundayDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.data.dto.SuggestedSongDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.data.dto.TopSongDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.data.dto.TopToneDto
import com.gabrielafonso.ipb.castelobranco.data.local.JsonSnapshotStorage
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.Song
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.SuggestedSong
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.SundayPlayPushItem
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.SundaySet
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.SundaySetItem
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.TopSong
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.TopTone
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.domain.repository.SongsRepository
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class SongsRepositoryImpl @Inject constructor(
    private val api: BackendApi,
    private val jsonStorage: JsonSnapshotStorage
) : SongsRepository {

    companion object {
        private const val KEY_SONGS_BY_SUNDAY = "songs_by_sunday"
        private const val KEY_TOP_SONGS = "top_songs"
        private const val KEY_TOP_TONES = "top_tones"
        private const val KEY_SUGGESTED_SONGS = "suggested_songs"
        private const val KEY_ALL_SONGS = "all_songs"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        encodeDefaults = true
    }

    private val songsBySundayRepo = object : BaseListSnapshotRepository<SongsBySundayDto, SundaySet>(
        json = json,
        jsonStorage = jsonStorage,
        dtoListSerializer = ListSerializer(SongsBySundayDto.serializer()),
        key = KEY_SONGS_BY_SUNDAY,
        tag = "observeSongsBySunday",
        fetchNetwork = { ifNoneMatch -> api.getSongsBySunday(ifNoneMatch) }
    ) {
        override fun mapToDomain(dto: List<SongsBySundayDto>): List<SundaySet> =
            dto.map { day ->
                SundaySet(
                    date = day.date,
                    songs = day.songs.map { s ->
                        SundaySetItem(
                            position = s.position,
                            title = s.title,
                            artist = s.artist,
                            tone = s.tone
                        )
                    }
                )
            }
    }

    private val topSongsRepo = object : BaseListSnapshotRepository<TopSongDto, TopSong>(
        json = json,
        jsonStorage = jsonStorage,
        dtoListSerializer = ListSerializer(TopSongDto.serializer()),
        key = KEY_TOP_SONGS,
        tag = "observeTopSongs",
        fetchNetwork = { ifNoneMatch -> api.getTopSongs(ifNoneMatch) }
    ) {
        override fun mapToDomain(dto: List<TopSongDto>): List<TopSong> =
            dto.map { TopSong(title = it.title, playCount = it.playCount) }
    }

    private val topTonesRepo = object : BaseListSnapshotRepository<TopToneDto, TopTone>(
        json = json,
        jsonStorage = jsonStorage,
        dtoListSerializer = ListSerializer(TopToneDto.serializer()),
        key = KEY_TOP_TONES,
        tag = "observeTopTones",
        fetchNetwork = { ifNoneMatch -> api.getTopTones(ifNoneMatch) }
    ) {
        override fun mapToDomain(dto: List<TopToneDto>): List<TopTone> =
            dto.map { TopTone(tone = it.tone, count = it.count) }
    }

    private val suggestedRepo = object : BaseListSnapshotRepository<SuggestedSongDto, SuggestedSong>(
        json = json,
        jsonStorage = jsonStorage,
        dtoListSerializer = ListSerializer(SuggestedSongDto.serializer()),
        key = KEY_SUGGESTED_SONGS,
        tag = "observeSuggestedSongs",
        fetchNetwork = { _ ->
            throw IllegalStateException(
                "Não buscar rede em observeSuggestedSongs(). Use refreshSuggestedSongs(fixedByPosition)."
            )
        }
    ) {
        override fun mapToDomain(dto: List<SuggestedSongDto>): List<SuggestedSong> =
            dto.map { s ->
                SuggestedSong(
                    id = s.id,
                    songId = s.song.id,
                    title = s.song.title,
                    artist = s.song.artist,
                    date = s.date,
                    tone = s.tone,
                    position = s.position
                )
            }.sortedBy { it.position }
    }

    private val allSongsRepo = object : BaseListSnapshotRepository<AllSongDto, Song>(
        json = json,
        jsonStorage = jsonStorage,
        dtoListSerializer = ListSerializer(AllSongDto.serializer()),
        key = KEY_ALL_SONGS,
        tag = "observeAllSongs",
        fetchNetwork = { ifNoneMatch -> api.getAllSongs(ifNoneMatch) }
    ) {
        override fun mapToDomain(dto: List<AllSongDto>): List<Song> =
            dto.map {
                Song(
                    id = it.id,
                    title = it.title,
                    artist = it.artist,
                    categoryName = it.categoryName
                )
            }
    }

    override fun observeSongsBySunday() = songsBySundayRepo.observeSnapshotList()
    override suspend fun refreshSongsBySunday() = songsBySundayRepo.refreshSnapshotList()

    override fun observeTopSongs() = topSongsRepo.observeSnapshotList()
    override suspend fun refreshTopSongs() = topSongsRepo.refreshSnapshotList()

    override fun observeTopTones() = topTonesRepo.observeSnapshotList()
    override suspend fun refreshTopTones() = topTonesRepo.refreshSnapshotList()

    override fun observeSuggestedSongs() = suggestedRepo.observeSnapshotList()

    override suspend fun refreshSuggestedSongs(): Boolean = refreshSuggestedSongs(emptyMap())

    override suspend fun refreshSuggestedSongs(fixedByPosition: Map<Int, Int>): Boolean {
        val fixedParam = fixedByPosition
            .toList()
            .sortedBy { (pos, _) -> pos }
            .joinToString(separator = ",") { (pos, playedId) -> "$pos:$playedId" }

        val response = api.getSuggestedSongs(
            ifNoneMatch = null,
            fixed = fixedParam.ifBlank { null }
        )
        if (!response.isSuccessful) return false

        val body = response.body() ?: return false

        val rawDtoJson = json.encodeToString(
            ListSerializer(SuggestedSongDto.serializer()),
            body
        )
        jsonStorage.save(KEY_SUGGESTED_SONGS, rawDtoJson)

        val newETag = response.headers()["ETag"]?.trim()
        if (!newETag.isNullOrBlank()) {
            jsonStorage.saveETag(KEY_SUGGESTED_SONGS, newETag)
        }

        suggestedRepo.refreshSnapshotList()
        return true
    }

    override fun observeAllSongs() = allSongsRepo.observeSnapshotList()
    override suspend fun refreshAllSongs() = allSongsRepo.refreshSnapshotList()

    override suspend fun pushSundayPlays(
        date: String,
        plays: List<SundayPlayPushItem>
    ): Boolean {
        val body = RegisterSundayPlaysRequestDto(
            date = date,
            plays = plays.map {
                RegisterSundayPlayItemDto(
                    songId = it.songId,
                    position = it.position,
                    tone = it.tone
                )
            }
        )

        val response = api.registerSundayPlays(body)
        if (response.isSuccessful) return true

        val rawError = response.errorBody()?.string()?.trim().orEmpty()
        val message =
            parseBackendDetail(rawError)
                ?: rawError.takeIf { it.isNotBlank() }
                ?: "Erro no servidor (${response.code()})."

        throw IllegalStateException(message)
    }

    private fun parseBackendDetail(rawJson: String): String? {
        if (rawJson.isBlank()) return null
        return runCatching {
            val el = json.parseToJsonElement(rawJson)
            val obj = el.jsonObject
            obj["detail"]?.jsonPrimitive?.content?.trim().takeIf { !it.isNullOrBlank() }
        }.getOrNull()
    }
}