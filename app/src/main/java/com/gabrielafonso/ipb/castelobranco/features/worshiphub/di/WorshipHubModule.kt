package com.gabrielafonso.ipb.castelobranco.features.worshiphub.di

import com.gabrielafonso.ipb.castelobranco.features.worshiphub.data.repository.SongsRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.domain.repository.SongsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WorshipHubModule {
    @Binds
    @Singleton
    abstract fun bindSongsRepository(impl: SongsRepositoryImpl): SongsRepository
}
