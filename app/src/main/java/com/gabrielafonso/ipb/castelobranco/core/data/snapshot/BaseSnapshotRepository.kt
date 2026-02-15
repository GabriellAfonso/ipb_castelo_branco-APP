package com.gabrielafonso.ipb.castelobranco.core.data.snapshot

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

abstract class BaseSnapshotRepository<Dto, Domain>(
    private val initialValue: Domain?,
    private val json: Json,
    private val jsonStorage: JsonSnapshotStorage,
    private val serializer: KSerializer<Dto>,
    private val key: String,
    private val tag: String,
    private val fetchNetwork: suspend (ifNoneMatch: String?) -> Response<Dto>
) {

    companion object {
        private const val TAG_PREFIX = "BaseSnapshotRepo"
    }

    private val bump = MutableStateFlow(0)

    protected abstract fun mapToDomain(dto: Dto): Domain

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(): Flow<Domain?> =
        bump.flatMapLatest {
            flow {
                emit(initialValue)

                val cachedJson = jsonStorage.loadOrNull(key)
                if (!cachedJson.isNullOrBlank()) {
                    val cachedDto = json.decodeFromString(serializer, cachedJson)
                    emit(mapToDomain(cachedDto))
                }

                refreshInternal { domain ->
                    emit(domain)
                }
            }.flowOn(Dispatchers.IO)
        }

    suspend fun refresh(): Boolean {
        val result = refreshInternal()
        bump.update { it + 1 }
        return result
    }

    private suspend fun refreshInternal(
        onSuccess: (suspend (Domain) -> Unit)? = null
    ): Boolean =
        try {
            val lastETag = jsonStorage.loadETagOrNull(key)
            val response = fetchNetwork(lastETag)

            when {
                response.code() == 304 -> true

                response.isSuccessful -> {
                    val body = response.body() ?: return false
                    val raw = json.encodeToString(serializer, body)
                    jsonStorage.save(key, raw)

                    response.headers()["ETag"]
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                        ?.let { jsonStorage.saveETag(key, it) }

                    onSuccess?.invoke(mapToDomain(body))
                    true
                }

                else -> jsonStorage.loadOrNull(key) != null
            }
        } catch (e: Exception) {
            Log.w("$TAG_PREFIX-$tag", "Falha na atualização ($tag)", e)
            jsonStorage.loadOrNull(key) != null
        }
}
