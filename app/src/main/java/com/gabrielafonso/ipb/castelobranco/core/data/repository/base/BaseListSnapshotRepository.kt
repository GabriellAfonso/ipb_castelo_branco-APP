package com.gabrielafonso.ipb.castelobranco.core.data.repository.base

import android.util.Log
import com.gabrielafonso.ipb.castelobranco.core.data.local.JsonSnapshotStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import retrofit2.Response

abstract class BaseListSnapshotRepository<Dto, Domain>(
    private val json: Json,
    private val jsonStorage: JsonSnapshotStorage,
    private val dtoListSerializer: KSerializer<List<Dto>>,
    private val key: String,
    private val tag: String,
    private val fetchNetwork: suspend (ifNoneMatch: String?) -> Response<List<Dto>>
) {
    companion object {
        private const val TAG_PREFIX = "BaseListSnapshotRepo"
    }

    private val bump = MutableStateFlow(0)

    protected abstract fun mapToDomain(dto: List<Dto>): List<Domain>

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeSnapshotList(): Flow<List<Domain>> =
        bump.flatMapLatest {
            flow {
                emit(emptyList())

                val cachedJson = runCatching { jsonStorage.loadOrNull(key) }
                    .onFailure { Log.e("$TAG_PREFIX-$tag", "Falha ao ler snapshot $key", it) }
                    .getOrNull()

                if (!cachedJson.isNullOrBlank()) {
                    runCatching {
                        val cachedDto = json.decodeFromString(dtoListSerializer, cachedJson)
                        emit(mapToDomain(cachedDto))
                    }.onFailure { e ->
                        Log.e("$TAG_PREFIX-$tag", "Falha ao parsear snapshot $key", e)
                    }
                }

                runCatching {
                    val lastETag = runCatching { jsonStorage.loadETagOrNull(key) }.getOrNull()
                    val response = fetchNetwork(lastETag)

                    when {
                        response.code() == 304 -> Log.d("$TAG_PREFIX-$tag", "$tag: 304 Not Modified")
                        response.isSuccessful -> {
                            val body = response.body()
                            if (body != null) {
                                val raw = json.encodeToString(dtoListSerializer, body)
                                jsonStorage.save(key, raw)

                                val newETag = response.headers()["ETag"]?.trim()
                                if (!newETag.isNullOrBlank()) jsonStorage.saveETag(key, newETag)

                                emit(mapToDomain(body))
                                Log.d("$TAG_PREFIX-$tag", "$tag: atualizou snapshot (200)")
                            } else {
                                Log.w("$TAG_PREFIX-$tag", "$tag: 200 sem body")
                            }
                        }
                        else -> Log.w("$TAG_PREFIX-$tag", "$tag: HTTP ${response.code()}")
                    }
                }.onFailure { e ->
                    Log.e("$TAG_PREFIX-$tag", "Falha na atualização da API ($tag)", e)
                }
            }.flowOn(Dispatchers.IO)
        }

    suspend fun refreshSnapshotList(): Boolean {
        val result = try {
            val lastETag = runCatching { jsonStorage.loadETagOrNull(key) }.getOrNull()
            val response = fetchNetwork(lastETag)

            when {
                response.code() == 304 -> {
                    Log.d("$TAG_PREFIX-$tag", "$tag: 304 Not Modified")
                    true
                }
                response.isSuccessful -> {
                    val body = response.body()
                    if (body == null) {
                        Log.w("$TAG_PREFIX-$tag", "$tag: 200 sem body")
                        false
                    } else {
                        val raw = json.encodeToString(dtoListSerializer, body)
                        jsonStorage.save(key, raw)

                        val newETag = response.headers()["ETag"]?.trim()
                        if (!newETag.isNullOrBlank()) jsonStorage.saveETag(key, newETag)

                        Log.d("$TAG_PREFIX-$tag", "$tag: salvou snapshot (200)")
                        true
                    }
                }
                else -> {
                    Log.w("$TAG_PREFIX-$tag", "$tag: HTTP ${response.code()}")
                    runCatching { !jsonStorage.loadOrNull(key).isNullOrBlank() }.getOrDefault(false)
                }
            }
        } catch (e: Exception) {
            Log.w("$TAG_PREFIX-$tag", "$tag: falhou rede, tentando ver snapshot", e)
            runCatching { !jsonStorage.loadOrNull(key).isNullOrBlank() }.getOrDefault(false)
        }

        bump.update { it + 1 }
        return result
    }
}
