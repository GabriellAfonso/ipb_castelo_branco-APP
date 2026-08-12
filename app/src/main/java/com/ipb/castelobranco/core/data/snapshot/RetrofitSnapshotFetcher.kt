package com.ipb.castelobranco.core.data.snapshot

import com.ipb.castelobranco.core.domain.error.toAppError
import com.ipb.castelobranco.core.domain.snapshot.NetworkResult
import com.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.ipb.castelobranco.core.network.error.toAppError
import retrofit2.Response
import timber.log.Timber

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
                        IllegalStateException("Empty body").toAppError()
                    )

                else -> NetworkResult.Failure(response.toAppError())
            }
        } catch (t: Throwable) {
            Timber.w(t, "Snapshot fetch failed")
            NetworkResult.Failure(t.toAppError())
        }
}
