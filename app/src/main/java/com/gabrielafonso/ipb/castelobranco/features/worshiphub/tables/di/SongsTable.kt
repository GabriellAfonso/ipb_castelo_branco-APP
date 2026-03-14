package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.di

import com.gabrielafonso.ipb.castelobranco.core.di.AuthLessRetrofit
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.api.SongsTableApi
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.repository.SongsRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.repository.SongsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class SongsTable {

    @Binds
    @Singleton
    abstract fun bindSongsRepository(impl: SongsRepositoryImpl): SongsRepository

    companion object {
        @Provides
        @Singleton
        fun provideSongsTableApi(
            @AuthLessRetrofit retrofit: Retrofit
        ): SongsTableApi = retrofit.create(SongsTableApi::class.java)
    }
}