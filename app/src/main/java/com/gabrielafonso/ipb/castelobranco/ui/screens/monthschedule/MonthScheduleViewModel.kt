// app/src/main/java/com/gabrielafonso/ipb/castelobranco/ui/screens/monthschedule/MonthScheduleViewModel.kt
package com.gabrielafonso.ipb.castelobranco.ui.screens.monthschedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.data.repository.MonthScheduleRepositoryImpl
import com.gabrielafonso.ipb.castelobranco.domain.model.MonthSchedule
import com.gabrielafonso.ipb.castelobranco.domain.repository.MonthScheduleRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MonthScheduleViewModel(
    private val repository: MonthScheduleRepository
) : ViewModel() {

    private val _monthSchedule = MutableStateFlow<MonthSchedule?>(null)
    val monthSchedule: StateFlow<MonthSchedule?> = _monthSchedule.asStateFlow()

    private val _isRefreshingMonthSchedule = MutableStateFlow(false)
    val isRefreshingMonthSchedule: StateFlow<Boolean> = _isRefreshingMonthSchedule.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeMonthSchedule().collect { _monthSchedule.value = it }
        }
    }

    fun refreshMonthSchedule(minDurationMs: Long = 1_000L) {
        viewModelScope.launch {
            if (_isRefreshingMonthSchedule.value) return@launch
            _isRefreshingMonthSchedule.value = true

            try {
                val refreshJob = async { repository.refreshMonthSchedule() }
                val minTimeJob = async { delay(minDurationMs) }

                refreshJob.await()
                minTimeJob.await()
            } finally {
                _isRefreshingMonthSchedule.value = false
            }
        }
    }
}
