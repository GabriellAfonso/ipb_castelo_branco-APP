package com.gabrielafonso.ipb.castelobranco.core.domain.snapshot

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BaseSnapshotRepositoryTest {

    // ---------- Fakes ----------

    private class FakeCache<T>(
        var value: T? = null,
        var etag: String? = null
    ) : SnapshotCache<T> {

        override suspend fun load(): T? = value
        override suspend fun loadETag(): String? = etag

        override suspend fun save(dto: T, etag: String?) {
            this.value = dto
            this.etag = etag
        }

        override suspend fun clear() {
            value = null
            etag = null
        }
    }

    private class FakeFetcher<T>(
        var result: NetworkResult<T>
    ) : SnapshotFetcher<T> {

        override suspend fun fetch(etag: String?): NetworkResult<T> = result
    }

    private class TestRepo(
        cache: SnapshotCache<Int>,
        fetcher: SnapshotFetcher<Int>,
    ) : BaseSnapshotRepository<Int, String>(
        cache = cache,
        fetcher = fetcher,
        mapper = { it.toString() },
        logger = Logger.Noop,
        tag = "TestRepo"
    )

    // ---------- observe() ----------

    @Test
    fun observe_emitsLoadingCacheAndUpdatedData_whenNetworkSucceeds() = runTest {
        val cache = FakeCache(value = 1, etag = "e1")
        val fetcher = FakeFetcher(NetworkResult.Success(body = 2, etag = "e2"))
        val repo = TestRepo(cache, fetcher)

        repo.observe().test {
            assertEquals(SnapshotState.Loading, awaitItem())
            assertEquals(SnapshotState.Data("1"), awaitItem())
            assertEquals(SnapshotState.Data("2"), awaitItem())
            awaitComplete()
        }

        assertEquals(2, cache.load())
        assertEquals("e2", cache.loadETag())
    }

    @Test
    fun observe_emitsLoadingAndCacheOnly_whenNotModified() = runTest {
        val cache = FakeCache(value = 1, etag = "e1")
        val fetcher = FakeFetcher<Int>(NetworkResult.NotModified)
        val repo = TestRepo(cache, fetcher)

        repo.observe().test {
            assertEquals(SnapshotState.Loading, awaitItem())
            assertEquals(SnapshotState.Data("1"), awaitItem())
            awaitComplete()
        }

        assertEquals(1, cache.load())
        assertEquals("e1", cache.loadETag())
    }

    @Test
    fun observe_emitsLoadingCacheAndError_whenNetworkFailsWithCache() = runTest {
        val error = RuntimeException("network error")
        val cache = FakeCache(value = 1, etag = "e1")
        val fetcher = FakeFetcher<Int>(NetworkResult.Failure(error))
        val repo = TestRepo(cache, fetcher)

        repo.observe().test {
            assertEquals(SnapshotState.Loading, awaitItem())
            assertEquals(SnapshotState.Data("1"), awaitItem())
            assertEquals(SnapshotState.Error(error), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun observe_emitsLoadingAndError_whenNetworkFailsWithoutCache() = runTest {
        val error = RuntimeException("network error")
        val cache = FakeCache<Int>(value = null, etag = null)
        val fetcher = FakeFetcher<Int>(NetworkResult.Failure(error))
        val repo = TestRepo(cache, fetcher)

        repo.observe().test {
            assertEquals(SnapshotState.Loading, awaitItem())
            assertEquals(SnapshotState.Error(error), awaitItem())
            awaitComplete()
        }
    }

    // ---------- refresh() ----------

    @Test
    fun refresh_returnsUpdated_andUpdatesCache_whenNetworkSucceeds() = runTest {
        val cache = FakeCache(value = 1, etag = "e1")
        val fetcher = FakeFetcher(NetworkResult.Success(body = 2, etag = "e2"))
        val repo = TestRepo(cache, fetcher)

        val result = repo.refresh()

        assertEquals(RefreshResult.Updated, result)
        assertEquals(2, cache.load())
        assertEquals("e2", cache.loadETag())
    }

    @Test
    fun refresh_returnsNotModified_whenNetworkNotModified() = runTest {
        val cache = FakeCache(value = 1, etag = "e1")
        val fetcher = FakeFetcher<Int>(NetworkResult.NotModified)
        val repo = TestRepo(cache, fetcher)

        val result = repo.refresh()

        assertEquals(RefreshResult.NotModified, result)
        assertEquals(1, cache.load())
        assertEquals("e1", cache.loadETag())
    }

    @Test
    fun refresh_returnsCacheUsed_whenNetworkFailsWithCache() = runTest {
        val cache = FakeCache(value = 1, etag = "e1")
        val fetcher = FakeFetcher<Int>(NetworkResult.Failure(RuntimeException()))
        val repo = TestRepo(cache, fetcher)

        val result = repo.refresh()

        assertEquals(RefreshResult.CacheUsed, result)
    }

    @Test
    fun refresh_returnsError_whenNetworkFailsWithoutCache() = runTest {
        val error = RuntimeException("fatal")
        val cache = FakeCache<Int>(value = null, etag = null)
        val fetcher = FakeFetcher<Int>(NetworkResult.Failure(error))
        val repo = TestRepo(cache, fetcher)

        val result = repo.refresh()

        assertTrue(result is RefreshResult.Error)
    }
}
