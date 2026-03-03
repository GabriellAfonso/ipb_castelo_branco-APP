package com.gabrielafonso.ipb.castelobranco.features.admin.schedule.di

import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.data.api.AdminScheduleApi
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.data.repository.AdminScheduleRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.domain.repository.AdminScheduleRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdminScheduleModule {

    @Binds
    @Singleton
    abstract fun bindAdminScheduleRepository(
        impl: AdminScheduleRepositoryImpl
    ): AdminScheduleRepository


}
