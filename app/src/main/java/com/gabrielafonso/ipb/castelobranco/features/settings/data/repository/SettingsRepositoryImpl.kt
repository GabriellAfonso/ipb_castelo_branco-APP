package com.gabrielafonso.ipb.castelobranco.features.settings.data.repository

import com.gabrielafonso.ipb.castelobranco.core.data.local.ThemePreferences
import com.gabrielafonso.ipb.castelobranco.features.settings.domain.model.ThemeMode
import com.gabrielafonso.ipb.castelobranco.features.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val themePreferences: ThemePreferences
) : SettingsRepository {

    override val themeModeFlow: Flow<ThemeMode> = themePreferences.themeModeFlow

    override suspend fun setThemeMode(mode: ThemeMode) {
        themePreferences.setThemeMode(mode)
    }

    override suspend fun setFollowSystem() {
        themePreferences.setFollowSystem()
    }

    override suspend fun setLightMode() {
        themePreferences.setLightMode()
    }

    override suspend fun setDarkMode() {
        themePreferences.setDarkMode()
    }
}