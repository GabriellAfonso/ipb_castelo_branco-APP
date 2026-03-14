package com.gabrielafonso.ipb.castelobranco.features.hymnal.di

import com.gabrielafonso.ipb.castelobranco.core.di.AuthLessRetrofit
import com.gabrielafonso.ipb.castelobranco.features.hymnal.data.api.HymnalApi
import com.gabrielafonso.ipb.castelobranco.features.hymnal.data.repository.HymnalRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.repository.HymnalRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class HymnalModule {

    @Binds
    @Singleton
    abstract fun bindHymnalRepository(impl: HymnalRepositoryImpl): HymnalRepository

    companion object {
        @Provides
        @Singleton
        fun provideHymnalApi(
            @AuthLessRetrofit retrofit: Retrofit
        ): HymnalApi = retrofit.create(HymnalApi::class.java)
    }
}