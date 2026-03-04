package com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.usecase

import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.model.Hymn
import javax.inject.Inject

class SearchHymnsUseCase @Inject constructor() {
    operator fun invoke(hymns: List<Hymn>, query: String): List<Hymn> {
        val q = query.trim()
        if (q.isBlank()) return hymns
        return hymns.filter { hymn ->
            hymn.number.contains(q, ignoreCase = true) ||
                hymn.title.contains(q, ignoreCase = true) ||
                hymn.lyrics.any { it.text.contains(q, ignoreCase = true) }
        }
    }
}
