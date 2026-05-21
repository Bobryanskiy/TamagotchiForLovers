package com.github.bobryanskiy.tamagotchiforlovers.di

import com.github.bobryanskiy.tamagotchiforlovers.data.util.SystemClock
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TimeModule {
    @Binds
    abstract fun bindClock(impl: SystemClock): Clock
}