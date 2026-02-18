package com.gabrielafonso.ipb.castelobranco.features.schedule.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.gabrielafonso.ipb.castelobranco.features.auth.data.local.AuthSession
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.repository.ProfileRepository
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.model.MonthSchedule
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    private val authSession: AuthSession,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _monthScheduleState =
        MutableStateFlow<SnapshotState<MonthSchedule>>(SnapshotState.Loading)

    val monthScheduleState: StateFlow<SnapshotState<MonthSchedule>> =
        _monthScheduleState.asStateFlow()

    private val _cachedMonthSchedule = MutableStateFlow<MonthSchedule?>(null)
    val cachedMonthSchedule: StateFlow<MonthSchedule?> = _cachedMonthSchedule.asStateFlow()

    private val _isRefreshingMonthSchedule = MutableStateFlow(false)
    val isRefreshingMonthSchedule: StateFlow<Boolean> = _isRefreshingMonthSchedule.asStateFlow()

    val canViewSchedule: StateFlow<Boolean> =
        combine(
            authSession.isLoggedInFlow,
            profileRepository.observeMeProfile()
        ) { loggedIn, profileState ->
            val isMember = (profileState as? SnapshotState.Data)?.value?.isMember == true
            loggedIn && isMember
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    init {
        viewModelScope.launch {
            repository.observeMonthSchedule()
                .collect { snapshotState ->
                    _monthScheduleState.value = snapshotState
                    if (snapshotState is SnapshotState.Data) {
                        _cachedMonthSchedule.value = snapshotState.value
                    }
                }
        }
    }

    fun refreshMonthSchedule(minDurationMs: Long = 500L) {
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