package com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.di

import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.data.repository.LyricsRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.domain.repository.LyricsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LyricsModule {

    @Binds
    @Singleton
    abstract fun bindLyricsRepository(impl: LyricsRepositoryImpl): LyricsRepository
}
