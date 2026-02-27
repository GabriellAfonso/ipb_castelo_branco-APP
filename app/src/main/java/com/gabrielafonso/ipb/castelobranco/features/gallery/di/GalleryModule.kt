package com.gabrielafonso.ipb.castelobranco.features.gallery.di

import android.content.Context
import com.gabrielafonso.ipb.castelobranco.features.gallery.data.local.GalleryPhotoStorage
import com.gabrielafonso.ipb.castelobranco.features.gallery.data.repository.GalleryRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.features.gallery.domain.repository.GalleryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GalleryModule {

    // O @Binds continua na classe abstrata
    @Binds
    @Singleton
    abstract fun bindGalleryRepository(impl: GalleryRepositoryImpl): GalleryRepository

    // O @Provides vai para um companion object
    companion object {
        @Provides
        @Singleton
        fun provideGalleryPhotoStorage(
            @ApplicationContext context: Context
        ): GalleryPhotoStorage {
            return GalleryPhotoStorage(context)
        }
    }
}