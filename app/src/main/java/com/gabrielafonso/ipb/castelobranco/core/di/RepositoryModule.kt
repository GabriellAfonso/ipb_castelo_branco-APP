package com.gabrielafonso.ipb.castelobranco.core.di

import com.gabrielafonso.ipb.castelobranco.features.schedule.data.repository.ScheduleRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.features.profile.data.repository.ProfileRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.features.settings.data.repository.SettingsRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.data.repository.SongsRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.repository.ScheduleRepository
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.repository.ProfileRepository
import com.gabrielafonso.ipb.castelobranco.features.settings.domain.repository.SettingsRepository
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.domain.repository.SongsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

//    @Binds
//    @Singleton
//    abstract fun bindHymnalRepository(impl: HymnalRepositoryImpl): HymnalRepository

//    @Binds
//    @Singleton
//    abstract fun bindSongsRepository(impl: SongsRepositoryImpl): SongsRepository

//    @Binds
//    @Singleton
//    abstract fun bindMonthScheduleRepository(impl: ScheduleRepositoryImpl): ScheduleRepository

//    @Binds
//    @Singleton
//    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

//    @Binds
//    @Singleton
//    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

//    @Binds
//    @Singleton
//    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository
}
