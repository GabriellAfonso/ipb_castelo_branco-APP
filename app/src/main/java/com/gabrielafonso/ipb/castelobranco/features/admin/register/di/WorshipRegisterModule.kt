package com.gabrielafonso.ipb.castelobranco.features.admin.register.di

import com.gabrielafonso.ipb.castelobranco.features.admin.register.data.repository.WorshipRegisterRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.features.admin.register.domain.repository.WorshipRegisterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WorshipRegisterModule {

    @Binds
    @Singleton
    abstract fun bindWorshipRegisterRepository(
        impl: WorshipRegisterRepositoryImpl
    ): WorshipRegisterRepository
}