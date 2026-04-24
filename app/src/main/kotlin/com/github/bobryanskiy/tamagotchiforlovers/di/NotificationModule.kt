package com.github.bobryanskiy.tamagotchiforlovers.di
import com.github.bobryanskiy.tamagotchiforlovers.data.notification.HybridNotificationScheduler
import com.github.bobryanskiy.tamagotchiforlovers.data.notification.NotificationScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    @Binds @Singleton
    abstract fun bindScheduler(impl: HybridNotificationScheduler): NotificationScheduler
}