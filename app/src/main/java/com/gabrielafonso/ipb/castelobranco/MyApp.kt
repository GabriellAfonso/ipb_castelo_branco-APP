
package com.gabrielafonso.ipb.castelobranco

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.gabrielafonso.ipb.castelobranco.core.data.local.ThemePreferences
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class MyApp : Application(){
    override fun onCreate() {
        super.onCreate()
        val prefs = ThemePreferences(applicationContext)
        val prefDark = runBlocking { prefs.darkModeFlow.first() } // Boolean?

        AppCompatDelegate.setDefaultNightMode(
            when (prefDark) {
                null -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                true -> AppCompatDelegate.MODE_NIGHT_YES
                false -> AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}
