package com.gabrielafonso.ipb.castelobranco.features.settings.data.repository

import com.gabrielafonso.ipb.castelobranco.core.data.local.ThemePreferences
import com.gabrielafonso.ipb.castelobranco.features.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val themePreferences: ThemePreferences
) : SettingsRepository {
    override val darkModeFlow: Flow<Boolean?> = themePreferences.darkModeFlow
    override suspend fun setDarkMode(value: Boolean) = themePreferences.setDarkMode(value)
}