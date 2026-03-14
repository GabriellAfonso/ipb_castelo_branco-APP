package com.gabrielafonso.ipb.castelobranco.core.di

import com.gabrielafonso.ipb.castelobranco.core.domain.auth.AuthEventBus
import com.gabrielafonso.ipb.castelobranco.core.domain.auth.AuthEventBusImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthCoreModule {

    @Binds
    @Singleton
    abstract fun bindAuthEventBus(impl: AuthEventBusImpl): AuthEventBus
}