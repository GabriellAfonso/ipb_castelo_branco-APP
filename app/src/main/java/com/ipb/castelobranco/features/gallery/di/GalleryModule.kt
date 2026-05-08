package com.ipb.castelobranco.features.gallery.di

import android.content.Context
import androidx.work.WorkManager
import com.ipb.castelobranco.core.di.AuthedRetrofit
import com.ipb.castelobranco.core.domain.startup.Preloadable
import com.ipb.castelobranco.features.gallery.data.api.GalleryApi
import com.ipb.castelobranco.features.gallery.data.local.GalleryPhotoStorage
import com.ipb.castelobranco.features.gallery.data.repository.GalleryRepositoryImpl
import com.ipb.castelobranco.features.gallery.domain.repository.GalleryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GalleryModule {

    @Binds
    @Singleton
    abstract fun bindGalleryRepository(impl: GalleryRepositoryImpl): GalleryRepository

    companion object {
        @Provides
        @Singleton
        fun provideGalleryPhotoStorage(
            @ApplicationContext context: Context
        ): GalleryPhotoStorage = GalleryPhotoStorage(context)

        @Provides
        @Singleton
        fun provideGalleryApi(
            @AuthedRetrofit retrofit: Retrofit
        ): GalleryApi = retrofit.create(GalleryApi::class.java)

        @Provides
        @Singleton
        fun provideWorkManager(
            @ApplicationContext context: Context
        ): WorkManager = WorkManager.getInstance(context)

        @Provides @IntoSet
        fun bindGalleryPreloadable(r: GalleryRepository): Preloadable = Preloadable { r.preload() }
    }
}