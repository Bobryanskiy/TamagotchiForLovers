package com.github.bobryanskiy.tamagotchiforlovers.core.util

import kotlinx.coroutines.flow.MutableStateFlow

inline fun <T> MutableStateFlow<T>.updateState(transform: (T) -> T) {
    while (true) {
        val current = value
        val next = transform(current)
        if (compareAndSet(current, next)) return
    }
}