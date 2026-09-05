package com.example

import android.app.Application
import com.example.di.AppContainer

class AuraApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = AppContainer(this)
    }

    companion object {
        lateinit var instance: AuraApplication
            private set
    }
}
