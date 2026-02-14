package com.gabrielafonso.ipb.castelobranco.features.schedule.di

import com.gabrielafonso.ipb.castelobranco.features.schedule.data.repository.ScheduleRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.repository.ScheduleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ScheduleModule {

    @Binds
    @Singleton
    abstract fun bindScheduleRepository(impl: ScheduleRepositoryImpl): ScheduleRepository
}
