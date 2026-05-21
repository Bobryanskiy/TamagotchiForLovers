package com.github.bobryanskiy.tamagotchiforlovers.data.util

import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemClock @Inject constructor() : Clock {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}