package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.di

import com.gabrielafonso.ipb.castelobranco.core.data.snapshot.SnapshotCacheFactory
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotCache
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.api.SongsTableApi
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.AllSongDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.SongsBySundayDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.SuggestedSongDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.TopSongDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.TopToneDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.fetcher.AllSongsSnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.fetcher.SongsBySundaySnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.fetcher.SuggestedSongsSnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.fetcher.TopSongsSnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.fetcher.TopTonesSnapshotFetcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.builtins.ListSerializer

@Module
@InstallIn(SingletonComponent::class)
object SongsTableSnapshotModule {

    @Provides
    fun provideAllSongsCache(factory: SnapshotCacheFactory): SnapshotCache<List<AllSongDto>> =
        factory.create(
            key = "tables_all_songs",
            serializer = ListSerializer(AllSongDto.serializer())
        )

    @Provides
    fun provideSongsBySundayCache(factory: SnapshotCacheFactory): SnapshotCache<List<SongsBySundayDto>> =
        factory.create(
            key = "tables_songs_by_sunday",
            serializer = ListSerializer(SongsBySundayDto.serializer())
        )

    @Provides
    fun provideTopSongsCache(factory: SnapshotCacheFactory): SnapshotCache<List<TopSongDto>> =
        factory.create(
            key = "tables_top_songs",
            serializer = ListSerializer(TopSongDto.serializer())
        )

    @Provides
    fun provideTopTonesCache(factory: SnapshotCacheFactory): SnapshotCache<List<TopToneDto>> =
        factory.create(
            key = "tables_top_tones",
            serializer = ListSerializer(TopToneDto.serializer())
        )

    @Provides
    fun provideSuggestedSongsCache(factory: SnapshotCacheFactory): SnapshotCache<List<SuggestedSongDto>> =
        factory.create(
            key = "tables_suggested_songs",
            serializer = ListSerializer(SuggestedSongDto.serializer())
        )

    @Provides
    fun provideAllSongsFetcher(api: SongsTableApi): SnapshotFetcher<List<AllSongDto>> =
        AllSongsSnapshotFetcher(api)

    @Provides
    fun provideSongsBySundayFetcher(api: SongsTableApi): SnapshotFetcher<List<SongsBySundayDto>> =
        SongsBySundaySnapshotFetcher(api)

    @Provides
    fun provideTopSongsFetcher(api: SongsTableApi): SnapshotFetcher<List<TopSongDto>> =
        TopSongsSnapshotFetcher(api)

    @Provides
    fun provideTopTonesFetcher(api: SongsTableApi): SnapshotFetcher<List<TopToneDto>> =
        TopTonesSnapshotFetcher(api)
    @Provides
    fun provideSuggestedSongsFetcher(api: SongsTableApi): SnapshotFetcher<List<SuggestedSongDto>> =
        SuggestedSongsSnapshotFetcher(api)
}