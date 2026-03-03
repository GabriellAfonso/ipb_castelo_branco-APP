package com.gabrielafonso.ipb.castelobranco.core.domain.snapshot

sealed class SnapshotState<out T> {

    data object Loading : SnapshotState<Nothing>()

    data class Data<T>(val value: T) : SnapshotState<T>()

    data class Error(
        val throwable: Throwable
    ) : SnapshotState<Nothing>()
}
