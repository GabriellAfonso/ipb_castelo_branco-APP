package com.gabrielafonso.ipb.castelobranco.features.settings.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val darkModeFlow: Flow<Boolean?>
    suspend fun setDarkMode(value: Boolean)
}