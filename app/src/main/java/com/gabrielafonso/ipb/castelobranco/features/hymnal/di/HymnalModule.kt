package com.gabrielafonso.ipb.castelobranco.features.hymnal.di

import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.repository.HymnalRepository
import com.gabrielafonso.ipb.castelobranco.features.hymnal.data.repository.HymnalRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HymnalModule {

    @Binds
    @Singleton
    abstract fun bindHymnalRepository(impl: HymnalRepositoryImpl): HymnalRepository
}
