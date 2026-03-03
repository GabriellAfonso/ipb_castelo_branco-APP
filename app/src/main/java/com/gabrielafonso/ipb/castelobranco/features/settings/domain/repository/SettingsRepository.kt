package com.gabrielafonso.ipb.castelobranco.features.settings.domain.repository

import com.gabrielafonso.ipb.castelobranco.features.settings.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    val themeModeFlow: Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setFollowSystem()
    suspend fun setLightMode()
    suspend fun setDarkMode()
}