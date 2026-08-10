package com.pavlo.regionsmapdownloader

import android.app.Application

class RegionApplication: Application() {
    lateinit var appInitializer: AppInitializer
        private set

    override fun onCreate() {
        super.onCreate()
        appInitializer = AppInitializer(this)
    }
}