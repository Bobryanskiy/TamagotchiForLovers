package com.github.bobryanskiy.tamagotchiforlovers

import android.app.Application
import com.github.bobryanskiy.tamagotchiforlovers.core.work.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TamagotchiApp : Application() {
    @Inject
    lateinit var syncScheduler: SyncScheduler

    override fun onCreate() {
        super.onCreate()
        syncScheduler.schedulePeriodicSync()
    }
}