package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.fetcher

import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.NetworkResult
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.api.SongsTableApi
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.api.SuggestedSongsFixedEncoder
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.SuggestedSongDto
import javax.inject.Inject

class SuggestedSongsFetcher @Inject constructor(
    private val api: SongsTableApi
) {
    suspend fun fetch(fixedByPosition: Map<Int, Int>): NetworkResult<List<SuggestedSongDto>> {
        return try {
            val response = api.getSuggestedSongs(
                ifNoneMatch = null,
                fixed = SuggestedSongsFixedEncoder.encode(fixedByPosition)
            )

            if (!response.isSuccessful) {
                NetworkResult.Failure(IllegalStateException("HTTP ${response.code()}"))
            } else {
                val body = response.body()
                if (body == null) {
                    NetworkResult.Failure(IllegalStateException("Resposta vazia"))
                } else {
                    NetworkResult.Success(
                        body = body,
                        etag = response.headers()["ETag"]?.trim()
                    )
                }
            }
        } catch (t: Throwable) {
            NetworkResult.Failure(t)
        }
    }
}