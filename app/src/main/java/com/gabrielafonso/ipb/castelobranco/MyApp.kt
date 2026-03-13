package com.gabrielafonso.ipb.castelobranco

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.gabrielafonso.ipb.castelobranco.core.data.local.ThemePreferences
import com.gabrielafonso.ipb.castelobranco.features.settings.domain.model.ThemeMode
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MyApp : Application() {

    @Inject lateinit var themePreferences: ThemePreferences

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        appScope.launch {
            val mode = runCatching { themePreferences.themeModeFlow.first() }
                .getOrDefault(ThemeMode.FOLLOW_SYSTEM)
            val nightMode = when (mode) {
                ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                ThemeMode.FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }
    }
}