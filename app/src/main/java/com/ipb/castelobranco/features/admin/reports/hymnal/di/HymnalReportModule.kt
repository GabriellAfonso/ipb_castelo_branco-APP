package com.ipb.castelobranco.features.admin.reports.hymnal.di

import com.ipb.castelobranco.core.di.AuthedRetrofit
import com.ipb.castelobranco.features.admin.reports.hymnal.data.api.HymnalHistoryAdminApi
import com.ipb.castelobranco.features.admin.reports.hymnal.data.api.HymnalReportApi
import com.ipb.castelobranco.features.admin.reports.hymnal.data.repository.HymnalHistoryAdminRepositoryImpl
import com.ipb.castelobranco.features.admin.reports.hymnal.data.repository.HymnalReportRepositoryImpl
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.repository.HymnalHistoryAdminRepository
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.repository.HymnalReportRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Everything here is administrator-only, so both APIs come from `@AuthedRetrofit` — **including
 * the settings read**. The service allows that read anonymously and the invisible collection in
 * `features/hymnal` uses the anonymous client for it, but this caller is an administrator and
 * the wrong qualifier here would be a silent 401 on every other call in the screen.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class HymnalReportModule {

    @Binds
    @Singleton
    abstract fun bindHymnalReportRepository(
        impl: HymnalReportRepositoryImpl,
    ): HymnalReportRepository

    @Binds
    @Singleton
    abstract fun bindHymnalHistoryAdminRepository(
        impl: HymnalHistoryAdminRepositoryImpl,
    ): HymnalHistoryAdminRepository

    companion object {

        @Provides
        @Singleton
        fun provideHymnalReportApi(
            @AuthedRetrofit retrofit: Retrofit,
        ): HymnalReportApi = retrofit.create(HymnalReportApi::class.java)

        @Provides
        @Singleton
        fun provideHymnalHistoryAdminApi(
            @AuthedRetrofit retrofit: Retrofit,
        ): HymnalHistoryAdminApi = retrofit.create(HymnalHistoryAdminApi::class.java)
    }
}
