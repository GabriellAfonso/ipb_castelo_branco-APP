package com.gabrielafonso.ipb.castelobranco.features.profile.di

import com.gabrielafonso.ipb.castelobranco.core.data.snapshot.SnapshotCacheFactory
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotCache
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.profile.data.api.ProfileApi
import com.gabrielafonso.ipb.castelobranco.features.profile.data.dto.MeProfileDto
import com.gabrielafonso.ipb.castelobranco.features.profile.data.snapshot.ProfileSnapshotFetcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object ProfileSnapshotModule {

    @Provides
    fun provideProfileSnapshotCache(
        factory: SnapshotCacheFactory
    ): SnapshotCache<MeProfileDto> =
        factory.create(
            key = "me_profile",
            serializer = MeProfileDto.serializer()
        )

    @Provides
    fun provideProfileSnapshotFetcher(
        api: ProfileApi
    ): SnapshotFetcher<MeProfileDto> =
        ProfileSnapshotFetcher(api)
}
