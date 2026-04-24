package com.github.bobryanskiy.tamagotchiforlovers.data.time

import com.github.bobryanskiy.tamagotchiforlovers.domain.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemClock @Inject constructor() : Clock {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}