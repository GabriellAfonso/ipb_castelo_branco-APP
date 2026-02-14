// app/src/main/java/com/gabrielafonso/ipb/castelobranco/core/di/NetworkModule.kt
package com.gabrielafonso.ipb.castelobranco.core.di

import com.gabrielafonso.ipb.castelobranco.BuildConfig
import com.gabrielafonso.ipb.castelobranco.core.network.AuthInterceptor
import com.gabrielafonso.ipb.castelobranco.core.network.TokenAuthenticator
import com.gabrielafonso.ipb.castelobranco.core.data.local.JsonSnapshotStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import android.content.Context
import com.gabrielafonso.ipb.castelobranco.features.auth.data.api.AuthApi
import com.gabrielafonso.ipb.castelobranco.features.hymnal.data.api.HymnalApi
import com.gabrielafonso.ipb.castelobranco.features.profile.data.api.ProfileApi
import com.gabrielafonso.ipb.castelobranco.features.schedule.data.api.ScheduleApi
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.api.SongsTableApi


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthLessRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthedRetrofit
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthLessClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Client

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiBaseUrl

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @ApiBaseUrl
    fun provideBaseUrl(): String = BuildConfig.API_BASE_URL

    @Provides
    @Singleton
    @AuthLessClient
    fun provideAuthLessOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @Client
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()


    @Provides
    @Singleton
    @AuthedRetrofit
    fun provideAuthedRetrofit(
        json: Json,
        @Client client: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(client)
            .build()

    @Provides
    @Singleton
    @AuthLessRetrofit
    fun provideAuthLessRetrofit(
        json: Json,
        @AuthLessClient client: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(client)
            .build()

    @Provides
    @Singleton
    fun provideProfileApi(
        @AuthedRetrofit retrofit: Retrofit
    ): ProfileApi =
        retrofit.create(ProfileApi::class.java)

    @Provides
    @Singleton
    fun provideAuthApi(
        @AuthLessRetrofit retrofit: Retrofit
    ): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideScheduleApi(
        @AuthLessRetrofit retrofit: Retrofit
    ): ScheduleApi =
        retrofit.create(ScheduleApi::class.java)

    @Provides
    @Singleton
    fun provideHymnalApi(
        @AuthLessRetrofit retrofit: Retrofit
    ): HymnalApi =
        retrofit.create(HymnalApi::class.java)

    @Provides
    @Singleton
    fun provideSongsTableApi(
        @AuthedRetrofit retrofit: Retrofit
    ): SongsTableApi =
        retrofit.create(SongsTableApi::class.java)
// colocar como authless dps que criar um repository pro register


    @Provides
    @Singleton
    fun provideJsonSnapshotStorage(@ApplicationContext context: Context): JsonSnapshotStorage =
        JsonSnapshotStorage(context)
}
