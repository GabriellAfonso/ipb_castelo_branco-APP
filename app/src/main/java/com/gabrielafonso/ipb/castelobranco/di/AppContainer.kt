// app/src/main/java/com/gabrielafonso/ipb/castelobranco/di/AppContainer.kt
package com.gabrielafonso.ipb.castelobranco.core.di

import android.content.Context
import com.gabrielafonso.ipb.castelobranco.BuildConfig
import com.gabrielafonso.ipb.castelobranco.data.api.SongsApi
import com.gabrielafonso.ipb.castelobranco.data.local.DatabaseProvider
import com.gabrielafonso.ipb.castelobranco.data.local.JsonSnapshotStorage
import com.gabrielafonso.ipb.castelobranco.data.repository.SongsRepositoryImpl
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .addConverterFactory(
            json.asConverterFactory("application/json".toMediaType())
        )
        .client(okHttpClient)
        .build()

    val songsApi: SongsApi = retrofit.create(SongsApi::class.java)

    private val db = DatabaseProvider.get(appContext)

    private val jsonSnapshotStorage = JsonSnapshotStorage(appContext)

    val songsRepository: SongsRepositoryImpl =
        SongsRepositoryImpl(
            api = songsApi,
            db = db,
            jsonStorage = jsonSnapshotStorage
        )
}
