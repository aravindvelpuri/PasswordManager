package com.aravindprojects.passwordmanager

import android.app.Application
import com.aravindprojects.passwordmanager.utils.FirebaseUtils

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseUtils.initialize(this)
    }
}