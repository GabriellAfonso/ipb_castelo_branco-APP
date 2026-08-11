package com.ipb.castelobranco.features.hymnal.di

import com.ipb.castelobranco.core.di.AuthLessRetrofit
import com.ipb.castelobranco.core.di.AuthedRetrofit
import com.ipb.castelobranco.core.domain.startup.Refreshable
import com.ipb.castelobranco.features.hymnal.data.api.HymnalHistoryApi
import com.ipb.castelobranco.features.hymnal.data.repository.HymnViewHistoryRepositoryImpl
import com.ipb.castelobranco.features.hymnal.data.work.WorkManagerHymnViewSyncScheduler
import com.ipb.castelobranco.features.hymnal.domain.repository.HymnViewHistoryRepository
import com.ipb.castelobranco.features.hymnal.domain.sync.HymnViewSyncScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * The two API instances differ only in whether the request may carry a token. Naming them
 * unambiguously matters: the authed one attached to a stale token can drive `TokenAuthenticator`
 * into a refresh-or-logout cycle from a background job.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthedHistoryApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthLessHistoryApi

@Module
@InstallIn(SingletonComponent::class)
abstract class HymnalHistoryModule {

    @Binds
    @Singleton
    abstract fun bindHymnViewHistoryRepository(
        impl: HymnViewHistoryRepositoryImpl
    ): HymnViewHistoryRepository

    @Binds
    @Singleton
    abstract fun bindHymnViewSyncScheduler(
        impl: WorkManagerHymnViewSyncScheduler
    ): HymnViewSyncScheduler

    companion object {

        @Provides
        @Singleton
        @AuthedHistoryApi
        fun provideAuthedHistoryApi(
            @AuthedRetrofit retrofit: Retrofit
        ): HymnalHistoryApi = retrofit.create(HymnalHistoryApi::class.java)

        @Provides
        @Singleton
        @AuthLessHistoryApi
        fun provideAuthLessHistoryApi(
            @AuthLessRetrofit retrofit: Retrofit
        ): HymnalHistoryApi = retrofit.create(HymnalHistoryApi::class.java)

        @Provides
        @IntoSet
        fun bindHistorySettingsRefreshable(
            r: HymnViewHistoryRepository
        ): Refreshable = Refreshable { r.refreshSettings() }
    }
}
