package com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.di

import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.data.repository.ChordChartRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.domain.repository.ChordChartRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChordChartsModule {

    @Binds
    @Singleton
    abstract fun bindChordChartRepository(impl: ChordChartRepositoryImpl): ChordChartRepository
}
