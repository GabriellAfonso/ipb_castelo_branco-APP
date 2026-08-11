package com.ipb.castelobranco.features.hymnal.domain.timer

import com.ipb.castelobranco.core.domain.util.MonotonicClock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HymnViewTimerTest {

    private lateinit var clock: FakeMonotonicClock
    private lateinit var timer: HymnViewTimer

    private val thresholdMillis = 30_000L

    @Before
    fun setUp() {
        clock = FakeMonotonicClock()
        timer = HymnViewTimer(clock)
    }

    @Test
    fun `accumulates foreground time across a background gap`() {
        timer.onVisible()
        clock.advance(20_000)
        timer.onHidden()

        // Ten minutes in the background must not count.
        clock.advance(600_000)

        timer.onVisible()
        clock.advance(15_000)

        assertEquals(35_000L, timer.elapsedMillis())
        assertEquals(0L, timer.remainingMillis(thresholdMillis))
    }

    @Test
    fun `remaining reflects only foreground time while backgrounded`() {
        timer.onVisible()
        clock.advance(20_000)
        timer.onHidden()

        clock.advance(600_000)

        assertEquals(20_000L, timer.elapsedMillis())
        assertEquals(10_000L, timer.remainingMillis(thresholdMillis))
    }

    @Test
    fun `does not reach the threshold below it`() {
        timer.onVisible()
        clock.advance(29_000)

        assertTrue(timer.remainingMillis(thresholdMillis) > 0)
        assertFalse(timer.hasFired)
    }

    @Test
    fun `markFired succeeds exactly once no matter how long the visit runs`() {
        timer.onVisible()
        clock.advance(30_000)

        assertTrue(timer.markFired())

        clock.advance(300_000)

        assertFalse(timer.markFired())
        assertFalse(timer.markFired())
        assertTrue(timer.hasFired)
    }

    @Test
    fun `backgrounded before the threshold and never returning stays below it`() {
        timer.onVisible()
        clock.advance(10_000)
        timer.onHidden()

        clock.advance(86_400_000)

        assertEquals(10_000L, timer.elapsedMillis())
        assertEquals(20_000L, timer.remainingMillis(thresholdMillis))
        assertFalse(timer.hasFired)
    }

    @Test
    fun `elapsed is never negative and remaining never goes below zero`() {
        timer.onVisible()
        clock.advance(45_000)

        assertTrue(timer.elapsedMillis() >= 0)
        assertEquals(0L, timer.remainingMillis(thresholdMillis))
    }

    @Test
    fun `repeated onVisible without onHidden does not restart the slice`() {
        timer.onVisible()
        clock.advance(10_000)
        timer.onVisible()
        clock.advance(10_000)

        assertEquals(20_000L, timer.elapsedMillis())
    }

    @Test
    fun `onHidden without onVisible is a no-op`() {
        timer.onHidden()

        assertEquals(0L, timer.elapsedMillis())
    }

    private class FakeMonotonicClock : MonotonicClock {
        private var now = 1_000_000L
        override fun elapsedMillis(): Long = now
        fun advance(millis: Long) { now += millis }
    }
}
