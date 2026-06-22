package com.ipb.castelobranco.core.data.logging

import android.util.Log
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class LogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: String,
    val priority: Int,
    val tag: String?,
    val message: String,
) {
    val levelLabel: String
        get() = when (priority) {
            Log.VERBOSE -> "V"
            Log.DEBUG -> "D"
            Log.INFO -> "I"
            Log.WARN -> "W"
            Log.ERROR -> "E"
            Log.ASSERT -> "A"
            else -> "?"
        }

    fun formatted(): String = "$timestamp $levelLabel/${tag ?: "---"}: $message"
}

object LogBufferTree : Timber.Tree() {

    private const val MAX_ENTRIES = 500
    private const val MAX_MESSAGE_LENGTH = 2000
    private val buffer = ArrayDeque<LogEntry>(MAX_ENTRIES)
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val entry = LogEntry(
            timestamp = dateFormat.format(Date()),
            priority = priority,
            tag = tag,
            message = buildString {
                append(message)
                if (t != null) {
                    append('\n')
                    append(t.stackTraceToString())
                }
            }.take(MAX_MESSAGE_LENGTH),
        )
        synchronized(buffer) {
            if (buffer.size >= MAX_ENTRIES) buffer.removeFirst()
            buffer.addLast(entry)
        }
    }

    fun snapshot(): List<LogEntry> = synchronized(buffer) { buffer.toList() }

    fun clear() = synchronized(buffer) { buffer.clear() }
}
