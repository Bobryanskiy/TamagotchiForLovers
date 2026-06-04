package com.github.bobryanskiy.tamagotchiforlovers.core.logging.impl

import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import timber.log.Timber
import javax.inject.Inject

class TimberAppLogger @Inject constructor() : AppLogger {
    override fun d(tag: String, message: String, throwable: Throwable?) {
        val tree = if (tag.isNotBlank()) Timber.tag(tag) else Timber
        if (throwable != null) tree.d(throwable, message) else tree.d(message)
    }

    override fun i(tag: String, message: String, throwable: Throwable?) {
        val tree = if (tag.isNotBlank()) Timber.tag(tag) else Timber
        if (throwable != null) tree.i(throwable, message) else tree.i(message)
    }

    override fun w(tag: String, message: String, throwable: Throwable?) {
        val tree = if (tag.isNotBlank()) Timber.tag(tag) else Timber
        if (throwable != null) tree.w(throwable, message) else tree.w(message)
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        val tree = if (tag.isNotBlank()) Timber.tag(tag) else Timber
        if (throwable != null) tree.e(throwable, message) else tree.e(message)
    }
}
