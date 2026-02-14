package com.gabrielafonso.ipb.castelobranco.features.settings.di


import com.gabrielafonso.ipb.castelobranco.features.settings.data.repository.SettingsRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.features.settings.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
