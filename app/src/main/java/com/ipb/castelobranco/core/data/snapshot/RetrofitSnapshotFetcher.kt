package com.ipb.castelobranco.core.data.snapshot

import com.ipb.castelobranco.core.domain.snapshot.NetworkResult
import com.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import retrofit2.Response

open class RetrofitSnapshotFetcher<T>(
    private val call: suspend (etag: String?) -> Response<T>
) : SnapshotFetcher<T> {

    override suspend fun fetch(etag: String?): NetworkResult<T> =
        try {
            val response = call(etag)

            when {
                response.code() == 304 ->
                    NetworkResult.NotModified

                response.isSuccessful ->
                    response.body()?.let {
                        NetworkResult.Success(it, response.headers()["ETag"])
                    } ?: NetworkResult.Failure(
                        IllegalStateException("Empty body")
                    )

                else ->
                    NetworkResult.Failure(
                        IllegalStateException("HTTP ${response.code()}")
                    )
            }
        } catch (t: Throwable) {
            NetworkResult.Failure(t)
        }
}
