package com.performily.flowboard.core.network

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Short-lived memory cache for reference data that many screens read, such as areas, positions
 * and the employee directory.
 *
 * Without it every screen asked the backend for the same lists again (the dashboard, the request
 * lists and the filters all load the directory), which made the app feel slow on a remote server.
 * Only successful results are kept, each for [TTL_MILLIS]; concurrent reads of the same key wait
 * for one request instead of firing several; and any write calls [invalidateAll] so the next read
 * brings fresh data.
 */
@Singleton
class ResponseCache @Inject constructor() {

    private class Entry(val value: Any, val storedAt: Long)

    private val entries = ConcurrentHashMap<String, Entry>()
    private val locks = ConcurrentHashMap<String, Mutex>()

    /**
     * Returns the cached value of [key] while it is fresh, otherwise loads it and keeps it on success.
     *
     * @param key identifier of the data, including any filter that changes the result.
     * @param load request that brings the data from the backend.
     * @return the cached or freshly loaded result.
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <T : Any> getOrLoad(key: String, load: suspend () -> Result<T>): Result<T> {
        fresh(key)?.let { return Result.success(it as T) }
        val lock = locks.getOrPut(key) { Mutex() }
        return lock.withLock {
            fresh(key)?.let { return@withLock Result.success(it as T) }
            load().onSuccess { value -> entries[key] = Entry(value, System.currentTimeMillis()) }
        }
    }

    /** Forgets every cached value, after any change to the data. */
    fun invalidateAll() {
        entries.clear()
    }

    /**
     * Value of [key] when it was stored less than [TTL_MILLIS] ago.
     *
     * @param key identifier of the data.
     * @return the value, or null when it is missing or expired.
     */
    private fun fresh(key: String): Any? {
        val entry = entries[key] ?: return null
        return if (System.currentTimeMillis() - entry.storedAt < TTL_MILLIS) entry.value else null
    }

    private companion object {
        /** Time a value stays valid: long enough to cover moving between screens, short enough to stay current. */
        const val TTL_MILLIS = 60_000L
    }
}
