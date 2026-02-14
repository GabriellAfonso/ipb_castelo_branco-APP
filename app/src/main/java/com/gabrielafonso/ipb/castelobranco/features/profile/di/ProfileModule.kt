package com.gabrielafonso.ipb.castelobranco.features.profile.di

import com.gabrielafonso.ipb.castelobranco.features.profile.data.repository.ProfileRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {
    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository
}
