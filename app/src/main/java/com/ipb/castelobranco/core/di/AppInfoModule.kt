package com.ipb.castelobranco.core.di

import android.os.SystemClock
import com.ipb.castelobranco.BuildConfig
import com.ipb.castelobranco.core.data.auth.AuthSessionStatusProvider
import com.ipb.castelobranco.core.data.local.DataStoreDeviceIdProvider
import com.ipb.castelobranco.core.data.local.DeviceIdProvider
import com.ipb.castelobranco.core.domain.auth.AuthStatusProvider
import com.ipb.castelobranco.core.domain.util.MonotonicClock
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppVersionName

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlatformName

/**
 * Ambient app facts and small cross-cutting primitives: the running version, the platform name,
 * a monotonic clock, the device identifier, and a read-only view of the session.
 *
 * These live here so `domain/` code can depend on plain values and interfaces instead of on
 * `BuildConfig`, `SystemClock`, or another feature's session class.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppInfoModule {

    @Binds
    @Singleton
    abstract fun bindDeviceIdProvider(impl: DataStoreDeviceIdProvider): DeviceIdProvider

    @Binds
    @Singleton
    abstract fun bindAuthStatusProvider(impl: AuthSessionStatusProvider): AuthStatusProvider

    companion object {

        @Provides
        @Singleton
        @AppVersionName
        fun provideAppVersionName(): String = BuildConfig.VERSION_NAME

        @Provides
        @Singleton
        @PlatformName
        fun providePlatformName(): String = PLATFORM_ANDROID

        @Provides
        @Singleton
        fun provideMonotonicClock(): MonotonicClock =
            MonotonicClock { SystemClock.elapsedRealtime() }

        private const val PLATFORM_ANDROID = "android"
    }
}
