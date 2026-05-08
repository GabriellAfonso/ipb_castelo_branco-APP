package com.ipb.castelobranco.features.bible.data.repository

import com.ipb.castelobranco.core.data.local.SnapshotStorage
import com.ipb.castelobranco.core.domain.snapshot.SnapshotCache
import com.ipb.castelobranco.features.bible.data.dto.BibleBookDto
import com.ipb.castelobranco.features.bible.data.local.BiblePreferences
import com.ipb.castelobranco.features.bible.domain.model.BibleBook
import com.ipb.castelobranco.features.bible.domain.model.BibleReadingPosition
import com.ipb.castelobranco.features.bible.domain.model.BibleTranslation
import com.ipb.castelobranco.features.bible.domain.repository.BibleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BibleRepositoryImpl @Inject constructor(
    private val caches: Map<BibleTranslation, @JvmSuppressWildcards SnapshotCache<List<BibleBookDto>>>,
    private val preferences: BiblePreferences,
    private val storage: SnapshotStorage,
) : BibleRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val booksMutex = Mutex()

    private val _booksFlow = MutableStateFlow<List<BibleBook>>(emptyList())
    override val booksFlow: StateFlow<List<BibleBook>> = _booksFlow.asStateFlow()

    private val _cachedTranslations = MutableStateFlow<Set<BibleTranslation>>(emptySet())
    override val cachedTranslationsFlow: StateFlow<Set<BibleTranslation>> = _cachedTranslations.asStateFlow()

    override val activeTranslationFlow: StateFlow<BibleTranslation> =
        preferences.translationFlow.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = BibleTranslation.Default,
        )

    override val positionFlow: StateFlow<BibleReadingPosition> =
        preferences.positionFlow.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = BibleReadingPosition.default(),
        )

    override val fontSizeFlow: StateFlow<Float> =
        preferences.fontSizeFlow.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = BiblePreferences.DEFAULT_FONT_SIZE,
        )

    init {
        // Recarrega livros sempre que a tradução ativa mudar.
        scope.launch {
            activeTranslationFlow.collect { translation ->
                loadBooksFor(translation)
            }
        }
    }

    override suspend fun preload() = withContext(Dispatchers.IO) {
        refreshCachedTranslations()
        loadBooksFor(activeTranslationFlow.value)
    }

    override suspend fun setActiveTranslation(translation: BibleTranslation) {
        preferences.setTranslation(translation)
        // O collector no init acima vai recarregar booksFlow.
    }

    override suspend fun savePosition(bookAbbrev: String, chapter: Int, verse: Int) {
        preferences.setPosition(bookAbbrev, chapter, verse)
    }

    override suspend fun setFontSize(size: Float) {
        preferences.setFontSize(size)
    }

    override suspend fun clearAll() = withContext(Dispatchers.IO) {
        BibleTranslation.entries.forEach { translation ->
            caches[translation]?.clear()
        }
        preferences.resetAll()
        _booksFlow.value = emptyList()
        _cachedTranslations.value = emptySet()
    }

    private suspend fun loadBooksFor(translation: BibleTranslation) {
        booksMutex.withLock {
            val cache = caches[translation] ?: return
            val cached = cache.load()
            _booksFlow.value = cached?.map { it.toDomain() } ?: emptyList()
            // Atualiza o conjunto de traduções já em disco.
            refreshCachedTranslations()
        }
    }

    private suspend fun refreshCachedTranslations() {
        val present = mutableSetOf<BibleTranslation>()
        for (t in BibleTranslation.entries) {
            val cache = caches[t] ?: continue
            if (cache.load() != null) present.add(t)
        }
        _cachedTranslations.value = present
    }
}

private fun BibleBookDto.toDomain(): BibleBook =
    BibleBook(abbrev = abbrev, name = name, chapters = chapters)
