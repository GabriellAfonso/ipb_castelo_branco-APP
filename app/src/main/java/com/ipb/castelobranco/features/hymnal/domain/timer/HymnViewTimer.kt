package com.ipb.castelobranco.features.hymnal.domain.timer

import com.ipb.castelobranco.core.domain.util.MonotonicClock

/**
 * Accumulates how long a hymn has been on screen **in the foreground**, across any number of
 * pauses, and latches once the threshold has been reported.
 *
 * Pure state: it neither schedules nor observes anything. The caller drives it with
 * [onVisible]/[onHidden] and decides when to check [remainingMillis]. One instance represents
 * one visit — a new visit gets a new instance.
 */
class HymnViewTimer(private val clock: MonotonicClock) {

    private var accumulatedMillis: Long = 0L
    private var visibleSinceMillis: Long? = null
    private var fired: Boolean = false

    /** True once [markFired] has succeeded — at most one event per visit. */
    val hasFired: Boolean get() = fired

    fun onVisible() {
        if (visibleSinceMillis == null) visibleSinceMillis = clock.elapsedMillis()
    }

    fun onHidden() {
        val since = visibleSinceMillis ?: return
        accumulatedMillis += (clock.elapsedMillis() - since).coerceAtLeast(0L)
        visibleSinceMillis = null
    }

    /** Foreground milliseconds so far, including the slice currently in progress. */
    fun elapsedMillis(): Long {
        val since = visibleSinceMillis ?: return accumulatedMillis
        return accumulatedMillis + (clock.elapsedMillis() - since).coerceAtLeast(0L)
    }

    /** How much longer this hymn must stay visible before it counts. Never negative. */
    fun remainingMillis(thresholdMillis: Long): Long =
        (thresholdMillis - elapsedMillis()).coerceAtLeast(0L)

    /**
     * Claims the single event this visit is allowed to produce.
     * Returns true exactly once; every later call returns false.
     */
    fun markFired(): Boolean {
        if (fired) return false
        fired = true
        return true
    }
}
