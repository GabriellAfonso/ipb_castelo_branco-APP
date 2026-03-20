package com.ipb.castelobranco.core.domain.auth

import kotlinx.coroutines.flow.SharedFlow

/**
 * Cross-feature event bus for authentication lifecycle events.
 * Lives in core so that feature modules can depend on it without
 * creating direct cross-feature dependencies.
 */
interface AuthEventBus {

    sealed class Event {
        /** Emitted immediately after a successful login or registration. */
        data object LoginSuccess : Event()
    }

    val events: SharedFlow<Event>

    fun emit(event: Event)
}