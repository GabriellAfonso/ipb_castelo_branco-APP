package com.gabrielafonso.ipb.castelobranco.features.auth.di

import com.gabrielafonso.ipb.castelobranco.features.auth.data.repository.AuthRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.features.auth.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}
