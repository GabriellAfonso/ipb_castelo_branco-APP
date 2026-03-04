package com.gabrielafonso.ipb.castelobranco.core.domain.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cross-feature event bus for authentication lifecycle events.
 * Lives in core so that feature modules can depend on it without
 * creating direct cross-feature dependencies.
 */
@Singleton
class AuthEventBus @Inject constructor() {

    sealed class Event {
        /** Emitted immediately after a successful login or registration. */
        data object LoginSuccess : Event()
    }

    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 1)
    val events: SharedFlow<Event> = _events.asSharedFlow()

    fun emit(event: Event) {
        _events.tryEmit(event)
    }
}
