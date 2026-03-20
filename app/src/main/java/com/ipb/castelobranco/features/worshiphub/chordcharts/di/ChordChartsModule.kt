package com.ipb.castelobranco.features.worshiphub.chordcharts.di

import com.ipb.castelobranco.core.di.AuthLessRetrofit
import com.ipb.castelobranco.features.worshiphub.chordcharts.data.api.ChordChartsApi
import com.ipb.castelobranco.features.worshiphub.chordcharts.data.repository.ChordChartRepositoryImpl
import com.ipb.castelobranco.features.worshiphub.chordcharts.domain.repository.ChordChartRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class ChordChartsModule {

    @Binds
    @Singleton
    abstract fun bindChordChartRepository(impl: ChordChartRepositoryImpl): ChordChartRepository

    companion object {
        @Provides
        @Singleton
        fun provideChordChartsApi(
            @AuthLessRetrofit retrofit: Retrofit
        ): ChordChartsApi = retrofit.create(ChordChartsApi::class.java)
    }
}