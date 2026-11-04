package com.example.setlistcompanion

import android.app.Application

// Custom Application class: created once when the app starts, before any screen.
// Registered in AndroidManifest.xml with android:name=".SetlistApp".
class SetlistApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
