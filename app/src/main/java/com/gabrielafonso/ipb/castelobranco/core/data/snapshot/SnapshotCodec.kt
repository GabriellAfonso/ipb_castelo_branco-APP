package com.gabrielafonso.ipb.castelobranco.core.data.snapshot

interface SnapshotCodec<T> {
    fun encode(value: T): String
    fun decode(raw: String): T
}
