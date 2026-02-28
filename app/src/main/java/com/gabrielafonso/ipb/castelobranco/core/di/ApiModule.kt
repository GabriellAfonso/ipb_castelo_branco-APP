package com.gabrielafonso.ipb.castelobranco.core.di

import com.gabrielafonso.ipb.castelobranco.features.admin.register.data.api.WorshipRegisterApi
import com.gabrielafonso.ipb.castelobranco.features.auth.data.api.AuthApi
import com.gabrielafonso.ipb.castelobranco.features.gallery.data.api.GalleryApi
import com.gabrielafonso.ipb.castelobranco.features.hymnal.data.api.HymnalApi
import com.gabrielafonso.ipb.castelobranco.features.profile.data.api.ProfileApi
import com.gabrielafonso.ipb.castelobranco.features.schedule.data.api.ScheduleApi
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.api.SongsTableApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideProfileApi(
        @AuthedRetrofit retrofit: Retrofit
    ): ProfileApi = retrofit.create(ProfileApi::class.java)

    @Provides
    @Singleton
    fun provideAuthApi(
        @AuthLessRetrofit retrofit: Retrofit
    ): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideScheduleApi(
       @AuthedRetrofit retrofit: Retrofit
    ): ScheduleApi = retrofit.create(ScheduleApi::class.java)

    @Provides
    @Singleton
    fun provideHymnalApi(
        @AuthLessRetrofit retrofit: Retrofit
    ): HymnalApi = retrofit.create(HymnalApi::class.java)

    @Provides
    @Singleton
    fun provideSongsTableApi(
        @AuthLessRetrofit retrofit: Retrofit
    ): SongsTableApi = retrofit.create(SongsTableApi::class.java)

    @Provides
    @Singleton
    fun provideWorshipRegisterApi(
        @AuthedRetrofit retrofit: Retrofit
    ): WorshipRegisterApi = retrofit.create(WorshipRegisterApi::class.java)

    @Provides
    @Singleton
    fun provideGalleryApi(
        @AuthedRetrofit retrofit: Retrofit
    ): GalleryApi = retrofit.create(GalleryApi::class.java)
}