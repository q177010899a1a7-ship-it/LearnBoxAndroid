package com.learnbox

import android.app.Application
import com.learnbox.data.db.AppDatabase

class LearnBoxApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        // No hardcoded API keys - users configure their own providers
    }
}
