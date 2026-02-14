package com.gabrielafonso.ipb.castelobranco.features.settings.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    val themeModeFlow: Flow<Int>

    suspend fun setThemeMode(mode: Int)

    suspend fun setFollowSystem()
    suspend fun setLightMode()
    suspend fun setDarkMode()
}