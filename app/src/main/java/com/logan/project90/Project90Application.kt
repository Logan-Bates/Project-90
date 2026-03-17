package com.logan.project90

import android.app.Application
import com.logan.project90.di.AppContainer
import com.logan.project90.di.DefaultAppContainer

class Project90Application : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = DefaultAppContainer(this)
    }
}
