package com.ipb.castelobranco.features.worshiphub.chordcharts.presentation.parser

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

    fun parse(content: String): List<ChordBlock> {
        val blocks = mutableListOf<ChordBlock>()
        var currentTitle: String? = null
        var currentIsIntro = false
        val currentLines = mutableListOf<ChordLine>()

        fun flushBlock() {
            if (currentTitle != null || currentIsIntro || currentLines.isNotEmpty()) {
                blocks += ChordBlock(currentTitle, currentIsIntro, currentLines.toList())
                currentLines.clear()
                currentTitle = null
                currentIsIntro = false
            }
        }

        for (rawLine in content.lines()) {
            val trimmed = rawLine.trim()
            val sectionMatch = SECTION_DIRECTIVE.matchEntire(trimmed)
            if (sectionMatch != null) {
                flushBlock()
                val directive = sectionMatch.groupValues[1]
                val name = sectionMatch.groupValues[2].trim()
                currentIsIntro = directive.equals("intro", ignoreCase = true)
                currentTitle = if (currentIsIntro || name.isEmpty()) null else name
            } else {
                currentLines += parseLine(rawLine)
            }
        }

        flushBlock()

        return blocks
    }

    private fun parseLine(line: String): ChordLine {
        val regex = Regex("""\[([^\]]+)\]""")
        val cleanText = regex.replace(line, "")

        val chords = mutableListOf<Pair<Int, String>>()
        var offset = 0
        for (match in regex.findAll(line)) {
            val posInClean = match.range.first - offset
            chords.add(Pair(posInClean, match.groupValues[1]))
            offset += match.value.length
        }

        if (chords.isEmpty()) {
            return ChordLine(if (cleanText.isNotEmpty()) listOf(LineToken.Lyrics(cleanText)) else emptyList())
        }

        val tokens = mutableListOf<LineToken>()

        if (cleanText.isEmpty()) {
            chords.forEach { (_, chord) ->
                tokens += LineToken.Chord(chord)
                tokens += LineToken.Lyrics(" ")
            }
            return ChordLine(tokens)
        }

        val segmentRegex = Regex("""\S+\s*|\s+""")
        val segments = segmentRegex.findAll(cleanText).toList()

        for (segment in segments) {
            val segStart = segment.range.first
            val segEnd   = segment.range.last
            val chord = chords
                .filter { it.first >= segStart && it.first <= segEnd }
                .minByOrNull { it.first }
                ?.second
            if (chord != null) tokens += LineToken.Chord(chord)
            tokens += LineToken.Lyrics(segment.value)
        }

        val lastSegEnd = segments.last().range.last
        chords.filter { it.first > lastSegEnd }.forEach { (_, chord) ->
            tokens += LineToken.Chord(chord)
            tokens += LineToken.Lyrics(" ")
        }

        return ChordLine(tokens)
    }

    private val SECTION_DIRECTIVE = Regex(
        """^\{(intro|verse|chorus|bridge|pre-chorus|interlude|outro)(?::\s*([^}]*))?\}$""",
        RegexOption.IGNORE_CASE,
    )
}