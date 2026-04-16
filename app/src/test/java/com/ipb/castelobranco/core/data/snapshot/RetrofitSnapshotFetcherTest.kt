package com.ipb.castelobranco.core.data.snapshot

import com.ipb.castelobranco.core.domain.snapshot.NetworkResult
import kotlinx.coroutines.test.runTest
import okhttp3.Headers
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class RetrofitSnapshotFetcherTest {

    // region 200 success

    @Test
    fun `fetch returns Success with body and ETag on 200 response`() = runTest {
        val fetcher = RetrofitSnapshotFetcher<String> {
            buildRetrofitResponse(code = 200, body = "payload", etag = "W/\"abc\"")
        }

        val result = fetcher.fetch(etag = null)

        assertTrue(result is NetworkResult.Success)
        val success = result as NetworkResult.Success
        assertEquals("payload", success.body)
        assertEquals("W/\"abc\"", success.etag)
    }

    @Test
    fun `fetch returns Success with null ETag when response has no ETag header`() = runTest {
        val fetcher = RetrofitSnapshotFetcher<String> {
            buildRetrofitResponse(code = 200, body = "payload", etag = null)
        }

        val result = fetcher.fetch(etag = null)

        assertTrue(result is NetworkResult.Success)
        assertNull((result as NetworkResult.Success).etag)
    }

    // endregion

    // region 304 not modified

    @Test
    fun `fetch returns NotModified on 304 response`() = runTest {
        val fetcher = RetrofitSnapshotFetcher<String> {
            buildRawResponse(code = 304)
        }

        val result = fetcher.fetch(etag = "W/\"abc\"")

        assertEquals(NetworkResult.NotModified, result)
    }

    // endregion

    // region empty body

    @Test
    fun `fetch returns Failure with IllegalStateException when body is null on 200`() = runTest {
        val fetcher = RetrofitSnapshotFetcher<String> {
            buildRetrofitResponse(code = 200, body = null, etag = null)
        }

        val result = fetcher.fetch(etag = null)

        assertTrue(result is NetworkResult.Failure)
        assertTrue((result as NetworkResult.Failure).throwable is IllegalStateException)
        assertEquals("Empty body", result.throwable.message)
    }

    // endregion

    // region http errors

    @Test
    fun `fetch returns Failure with HTTP error message on 4xx or 5xx response`() = runTest {
        val fetcher = RetrofitSnapshotFetcher<String> {
            buildRawResponse(code = 500)
        }

        val result = fetcher.fetch(etag = null)

        assertTrue(result is NetworkResult.Failure)
        val failure = result as NetworkResult.Failure
        assertTrue(failure.throwable is IllegalStateException)
        assertEquals("HTTP 500", failure.throwable.message)
    }

    // endregion

    // region network exception

    @Test
    fun `fetch returns Failure wrapping the thrown exception`() = runTest {
        val cause = RuntimeException("network timeout")
        val fetcher = RetrofitSnapshotFetcher<String> { throw cause }

        val result = fetcher.fetch(etag = null)

        assertTrue(result is NetworkResult.Failure)
        assertEquals(cause, (result as NetworkResult.Failure).throwable)
    }

    // endregion

    // region helpers

    private fun buildRetrofitResponse(
        code: Int,
        body: String?,
        etag: String?,
    ): Response<String> {
        if (body != null && code in 200..299) {
            val rawResponse = okhttp3.Response.Builder()
                .request(Request.Builder().url("https://api.example.com/").build())
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("OK")
                .apply { if (etag != null) header("ETag", etag) }
                .body("".toResponseBody())
                .build()
            return Response.success(body, rawResponse)
        }
        return Response.success(null as String?)
    }

    private fun buildRawResponse(code: Int): Response<String> {
        val raw = okhttp3.Response.Builder()
            .request(Request.Builder().url("https://api.example.com/").build())
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("")
            .body("".toResponseBody())
            .build()
        return Response.error("".toResponseBody(), raw)
    }

    // endregion
}
