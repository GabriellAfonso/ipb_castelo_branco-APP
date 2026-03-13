package com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.presentation.parser

data class LyricsPage(val stanzas: List<LyricsStanza>)

data class LyricsStanza(val lines: List<String>)

object LyricsParser {

    fun parse(content: String): List<LyricsPage> {
        val pages = mutableListOf<LyricsPage>()
        var currentStanzas: MutableList<LyricsStanza>? = null
        val currentLines = mutableListOf<String>()

        fun flushStanza() {
            val stanzas = currentStanzas ?: return
            if (currentLines.isNotEmpty()) {
                stanzas += LyricsStanza(currentLines.toList())
                currentLines.clear()
            }
        }

        for (rawLine in content.lines()) {
            val trimmed = rawLine.trim()
            when {
                START_PAGE.matches(trimmed) -> {
                    currentStanzas = mutableListOf()
                    currentLines.clear()
                }
                END_PAGE.matches(trimmed) -> {
                    flushStanza()
                    currentStanzas?.let { pages += LyricsPage(it.toList()) }
                    currentStanzas = null
                }
                currentStanzas == null -> continue
                trimmed.isEmpty() -> flushStanza()
                else -> currentLines += trimmed
            }
        }

        return pages
    }

    private val START_PAGE = Regex("""^\{p_ini\}$""", RegexOption.IGNORE_CASE)
    private val END_PAGE   = Regex("""^\{p_end\}$""", RegexOption.IGNORE_CASE)
}
