package com.gabrielafonso.ipb.castelobranco.features.profile.di

import com.gabrielafonso.ipb.castelobranco.core.di.AuthedRetrofit
import com.gabrielafonso.ipb.castelobranco.features.profile.data.api.ProfileApi
import com.gabrielafonso.ipb.castelobranco.features.profile.data.repository.ProfileRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    companion object {
        @Provides
        @Singleton
        fun provideProfileApi(
            @AuthedRetrofit retrofit: Retrofit
        ): ProfileApi = retrofit.create(ProfileApi::class.java)
    }
}