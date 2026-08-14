package com.ipb.castelobranco.core.domain.util

import java.time.LocalDate

/**
 * Today, in the church's local time.
 *
 * Exists for the same reason [MonotonicClock] does: `domain/` code must be able to ask what day
 * it is without calling `LocalDate.now()`, so every period resolution and every "há mais de um
 * ano" statement can be tested against a fixed day instead of the system clock.
 */
fun interface DateProvider {
    fun today(): LocalDate
}
