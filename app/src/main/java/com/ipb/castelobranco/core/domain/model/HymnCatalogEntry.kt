package com.ipb.castelobranco.core.domain.model

/**
 * A hymn as the rest of the app needs to know it: a number and a title.
 *
 * Exists so a feature outside `features/hymnal` can read the catalogue without importing it —
 * the same shape [com.ipb.castelobranco.core.domain.model.Song] plays for the worship hub.
 * Lyrics and the nullable server id stay inside the hymnal feature, where they mean something.
 */
data class HymnCatalogEntry(
    val number: String,
    val title: String,
)
