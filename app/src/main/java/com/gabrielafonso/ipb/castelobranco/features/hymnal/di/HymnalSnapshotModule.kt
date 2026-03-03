package com.gabrielafonso.ipb.castelobranco.features.hymnal.di

import com.gabrielafonso.ipb.castelobranco.core.data.snapshot.SnapshotCacheFactory
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotCache
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.hymnal.data.api.HymnalApi
import com.gabrielafonso.ipb.castelobranco.features.hymnal.data.dto.HymnDto
import com.gabrielafonso.ipb.castelobranco.features.hymnal.data.snapshot.HymnalSnapshotFetcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.builtins.ListSerializer

@Module
@InstallIn(SingletonComponent::class)
object HymnalSnapshotModule {

    @Provides
    fun provideHymnalSnapshotCache(
        factory: SnapshotCacheFactory
    ): SnapshotCache<List<HymnDto>> =
        factory.create(
            key = "hymnal",
            serializer = ListSerializer(HymnDto.serializer())
        )

    @Provides
    fun provideHymnalSnapshotFetcher(
        api: HymnalApi
    ): SnapshotFetcher<List<HymnDto>> =
        HymnalSnapshotFetcher(api)
}
