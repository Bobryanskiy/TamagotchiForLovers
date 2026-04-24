package com.github.bobryanskiy.tamagotchiforlovers

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// 🔑 Эта аннотация генерирует весь код для внедрения зависимостей
@HiltAndroidApp
class TamagotchiApp : Application() {
    // Здесь можно инициализировать Firebase, Timber и т.д.
    override fun onCreate() {
        super.onCreate()
        // Пример: FirebaseApp.initializeApp(this)
    }
}