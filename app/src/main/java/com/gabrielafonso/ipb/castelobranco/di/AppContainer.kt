package com.gabrielafonso.ipb.castelobranco.core.di

import android.content.Context
import com.gabrielafonso.ipb.castelobranco.BuildConfig
import com.gabrielafonso.ipb.castelobranco.data.api.BackendApi
import com.gabrielafonso.ipb.castelobranco.data.local.JsonSnapshotStorage
import com.gabrielafonso.ipb.castelobranco.data.repository.HymnalRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.data.repository.MonthScheduleRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.data.repository.SongsRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.domain.repository.HymnalRepository
import com.gabrielafonso.ipb.castelobranco.domain.repository.MonthScheduleRepository
import com.gabrielafonso.ipb.castelobranco.domain.repository.SongsRepository
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

    val backendApi: BackendApi = retrofit.create(BackendApi::class.java)

    private val jsonSnapshotStorage = JsonSnapshotStorage(appContext)

    val songsRepository: SongsRepository = SongsRepositoryImpl(
        api = backendApi,
        jsonStorage = jsonSnapshotStorage
    )

    val hymnalRepository: HymnalRepository = HymnalRepositoryImpl(
        api = backendApi,
        jsonStorage = jsonSnapshotStorage
    )

    val monthScheduleRepository: MonthScheduleRepository = MonthScheduleRepositoryImpl(
        api = backendApi,
        jsonStorage = jsonSnapshotStorage
    )
}
