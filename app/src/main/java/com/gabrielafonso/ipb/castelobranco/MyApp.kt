package com.gabrielafonso.ipb.castelobranco

import android.app.Application
import com.gabrielafonso.ipb.castelobranco.core.di.AppContainer

class MyApp : Application() {

    val appContainer: AppContainer by lazy {
        AppContainer(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Inicialização já acontece no lazy
    }
}