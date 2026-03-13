package com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.di

import com.gabrielafonso.ipb.castelobranco.core.data.snapshot.SnapshotCacheFactory
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotCache
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.data.api.LyricsApi
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.data.dto.LyricsDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.data.fetcher.LyricsSnapshotFetcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.builtins.ListSerializer

@Module
@InstallIn(SingletonComponent::class)
object LyricsSnapshotModule {

    @Provides
    fun provideLyricsCache(
        factory: SnapshotCacheFactory,
    ): SnapshotCache<List<LyricsDto>> = factory.create(
        key        = "worshiphub_lyrics",
        serializer = ListSerializer(LyricsDto.serializer()),
    )

    @Provides
    fun provideLyricsFetcher(
        api: LyricsApi,
    ): SnapshotFetcher<List<LyricsDto>> = LyricsSnapshotFetcher(api)
}
