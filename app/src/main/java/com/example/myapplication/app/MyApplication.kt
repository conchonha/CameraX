package com.example.myapplication.app

import android.app.Application
import androidx.lifecycle.ViewModelProvider

class MyApplication : Application() {
    companion object{
        lateinit var application : Application
        lateinit var factory : ViewModelProvider.Factory
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        factory = ViewModelProvider.AndroidViewModelFactory(this)
    }
}