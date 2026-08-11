package com.ipb.castelobranco.features.hymnal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipb.castelobranco.core.domain.util.MonotonicClock
import com.ipb.castelobranco.features.hymnal.domain.model.HymnViewCollectionSettings
import com.ipb.castelobranco.features.hymnal.domain.timer.HymnViewTimer
import com.ipb.castelobranco.features.hymnal.domain.usecase.GetHymnViewSettingsUseCase
import com.ipb.castelobranco.features.hymnal.domain.usecase.RecordHymnViewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Measures how long a hymn stays on screen and records one view once it qualifies.
 *
 * Scoped to the hymn detail *destination*, not to the hymnal graph, so leaving the hymn and
 * coming back later starts a fresh count while a configuration change keeps the current one.
 *
 * Exposes no state. The feature is invisible: nothing here reaches the UI.
 */
@HiltViewModel
class HymnViewTrackingViewModel @Inject constructor(
    private val recordHymnView: RecordHymnViewUseCase,
    private val getSettings: GetHymnViewSettingsUseCase,
    clock: MonotonicClock,
) : ViewModel() {

    private val timer = HymnViewTimer(clock)

    private var thresholdJob: Job? = null
    private var thresholdMillis: Long? = null
    private var currentHymnId: Int? = null

    fun onHymnVisible(hymnId: Int?) {
        if (timer.hasFired) return

        currentHymnId = hymnId
        timer.onVisible()

        thresholdJob?.cancel()
        thresholdJob = viewModelScope.launch {
            // Read once per visit: a settings change arriving mid-visit must not retroactively
            // alter a count already under way.
            val threshold = thresholdMillis ?: resolveThresholdMillis().also { thresholdMillis = it }

            val remaining = timer.remainingMillis(threshold)
            if (remaining > 0) delay(remaining)

            if (timer.markFired()) {
                recordHymnView(currentHymnId, timer.elapsedMillis() / MILLIS_PER_SECOND)
            }
        }
    }

    fun onHymnHidden() {
        thresholdJob?.cancel()
        thresholdJob = null
        timer.onHidden()
    }

    override fun onCleared() {
        super.onCleared()
        thresholdJob?.cancel()
    }

    private suspend fun resolveThresholdMillis(): Long {
        val seconds = runCatching { getSettings().minSecondsToCount }
            .getOrDefault(HymnViewCollectionSettings.DEFAULT_MIN_SECONDS_TO_COUNT)
        return seconds.toLong() * MILLIS_PER_SECOND
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1_000L
    }
}
