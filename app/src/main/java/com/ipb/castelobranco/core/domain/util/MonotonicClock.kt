package com.ipb.castelobranco.core.domain.util

/**
 * A monotonic time source, in milliseconds since an arbitrary origin.
 *
 * Only differences are meaningful. Used instead of wall-clock time wherever an elapsed duration
 * is measured, so a clock adjustment mid-measurement cannot produce a negative result.
 */
fun interface MonotonicClock {
    fun elapsedMillis(): Long
}
