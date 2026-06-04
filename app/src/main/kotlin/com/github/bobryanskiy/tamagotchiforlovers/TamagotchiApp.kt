package com.github.bobryanskiy.tamagotchiforlovers

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.github.bobryanskiy.tamagotchiforlovers.core.work.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class TamagotchiApp : Application(), Configuration.Provider {

    @Inject
    lateinit var syncScheduler: SyncScheduler

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(
                if (BuildConfig.DEBUG) android.util.Log.DEBUG
                else android.util.Log.ERROR
            )
            .build()

    override fun onCreate() {
        super.onCreate()

        // 1. Логирование
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // 3. Планируем периодическую синхронизацию
        syncScheduler.schedulePeriodicSync()
    }
}
