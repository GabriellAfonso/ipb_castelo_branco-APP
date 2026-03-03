package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.di

import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.repository.SongsRepository
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.repository.SongsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SongsTable {
    @Binds
    @Singleton
    abstract fun bindSongsRepository(impl: SongsRepositoryImpl): SongsRepository
}