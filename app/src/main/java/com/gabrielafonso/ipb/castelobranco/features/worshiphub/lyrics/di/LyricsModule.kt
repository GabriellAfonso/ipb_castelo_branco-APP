package com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.di

import com.gabrielafonso.ipb.castelobranco.core.di.AuthLessRetrofit
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.data.api.LyricsApi
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.data.repository.LyricsRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.domain.repository.LyricsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class LyricsModule {

    @Binds
    @Singleton
    abstract fun bindLyricsRepository(impl: LyricsRepositoryImpl): LyricsRepository

    companion object {
        @Provides
        @Singleton
        fun provideLyricsApi(
            @AuthLessRetrofit retrofit: Retrofit
        ): LyricsApi = retrofit.create(LyricsApi::class.java)
    }
}