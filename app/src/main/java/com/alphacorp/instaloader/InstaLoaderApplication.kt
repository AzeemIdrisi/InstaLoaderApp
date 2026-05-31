package com.alphacorp.instaloader

import android.app.Application
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class InstaLoaderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
    }
}
