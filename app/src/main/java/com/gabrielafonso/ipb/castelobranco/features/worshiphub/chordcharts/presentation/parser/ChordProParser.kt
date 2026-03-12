package com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.presentation.parser

data class ChordPage(val blocks: List<ChordBlock>)

data class ChordBlock(
    val title: String?,
    val isIntro: Boolean = false,
    val lines: List<ChordLine>,
)

data class ChordLine(val tokens: List<LineToken>)

sealed class LineToken {
    data class Chord(val value: String) : LineToken()
    data class Lyrics(val value: String) : LineToken()
}

object ChordProParser {

    fun parse(content: String): List<ChordPage> {
        val pages = mutableListOf<ChordPage>()
        var currentBlocks: MutableList<ChordBlock>? = null
        var currentTitle: String? = null
        var currentIsIntro = false
        val currentLines = mutableListOf<ChordLine>()

        fun flushBlock() {
            val blocks = currentBlocks ?: return
            if (currentTitle != null || currentIsIntro || currentLines.isNotEmpty()) {
                blocks += ChordBlock(currentTitle, currentIsIntro, currentLines.toList())
                currentLines.clear()
                currentTitle = null
                currentIsIntro = false
            }
        }

        for (rawLine in content.lines()) {
            val trimmed = rawLine.trim()
            when {
                START_PAGE.matches(trimmed) -> {
                    currentBlocks = mutableListOf()
                    currentTitle = null
                    currentIsIntro = false
                    currentLines.clear()
                }
                END_PAGE.matches(trimmed) -> {
                    flushBlock()
                    currentBlocks?.let { pages += ChordPage(it.toList()) }
                    currentBlocks = null
                }
                currentBlocks == null -> continue
                METADATA_DIRECTIVE.matches(trimmed) -> continue
                else -> {
                    val sectionMatch = SECTION_DIRECTIVE.matchEntire(trimmed)
                    if (sectionMatch != null) {
                        flushBlock()
                        val directive = sectionMatch.groupValues[1]
                        val name = sectionMatch.groupValues[2].trim()
                        currentIsIntro = directive.equals("intro", ignoreCase = true)
                        currentTitle = if (currentIsIntro) null
                                       else name.ifEmpty { directive.replaceFirstChar { it.uppercase() } }
                    } else {
                        currentLines += parseLine(rawLine)
                    }
                }
            }
        }

        return pages
    }

    private fun parseLine(line: String): ChordLine {
        val tokens = mutableListOf<LineToken>()
        val regex = Regex("""\[([^\]]+)\]""")
        var lastEnd = 0

        for (match in regex.findAll(line)) {
            if (match.range.first > lastEnd) {
                val text = line.substring(lastEnd, match.range.first)
                if (text.isNotEmpty()) tokens += LineToken.Lyrics(text)
            }
            tokens += LineToken.Chord(match.groupValues[1])
            lastEnd = match.range.last + 1
        }

        if (lastEnd < line.length) {
            val text = line.substring(lastEnd)
            if (text.isNotEmpty()) tokens += LineToken.Lyrics(text)
        }

        return ChordLine(tokens)
    }

    private val START_PAGE = Regex("""^\{p_ini\}$""", RegexOption.IGNORE_CASE)
    private val END_PAGE   = Regex("""^\{p_end\}$""", RegexOption.IGNORE_CASE)
    private val SECTION_DIRECTIVE = Regex(
        """^\{(intro|verse|chorus|bridge|pre-chorus|outro)(?::\s*([^}]*))?\}$""",
        RegexOption.IGNORE_CASE,
    )
    private val METADATA_DIRECTIVE = Regex(
        """^\{(?:title|key|capo|tempo|time|comment)[^}]*\}$""",
        RegexOption.IGNORE_CASE,
    )
}
