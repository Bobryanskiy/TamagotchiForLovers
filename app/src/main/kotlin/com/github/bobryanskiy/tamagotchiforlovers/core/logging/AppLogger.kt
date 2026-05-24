package com.github.bobryanskiy.tamagotchiforlovers.core.logging

/**
 * Абстракция логирования для всего приложения.
 *
 * Реализации:
 * - Debug: Timber
 * - Release: Timber + Crashlytics
 * - Test: InMemoryLogger
 */
interface AppLogger {
    fun d(tag: String, message: String, throwable: Throwable? = null)
    fun i(tag: String, message: String, throwable: Throwable? = null)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)

    companion object
}

fun AppLogger.d(message: String, throwable: Throwable? = null) {
    d(tag = "", message = message, throwable = throwable)
}