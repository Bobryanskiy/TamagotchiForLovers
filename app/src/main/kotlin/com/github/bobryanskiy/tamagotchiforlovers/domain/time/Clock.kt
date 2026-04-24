package com.github.bobryanskiy.tamagotchiforlovers.domain.time

interface Clock {
    fun currentTimeMillis(): Long
}