package com.ipb.castelobranco.core.data.snapshot

import com.ipb.castelobranco.core.domain.snapshot.HttpPermissionException
import com.ipb.castelobranco.core.domain.snapshot.NetworkResult
import com.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import org.json.JSONObject
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

                else -> {
                    val code = response.code()
                    val errorMessage = try {
                        val raw = response.errorBody()?.string()
                        raw?.let { JSONObject(it).optString("detail", it) } ?: "HTTP $code"
                    } catch (_: Exception) {
                        "HTTP $code"
                    }
                    val exception = if (code == 401 || code == 403) {
                        HttpPermissionException(code, errorMessage)
                    } else {
                        IllegalStateException(errorMessage)
                    }
                    NetworkResult.Failure(exception)
                }
            }
        } catch (t: Throwable) {
            NetworkResult.Failure(t)
        }
}
