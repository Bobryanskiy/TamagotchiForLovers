package com.github.bobryanskiy.tamagotchiforlovers.domain.util

sealed interface Loadable<out T> {
    data object Loading : Loadable<Nothing>
    data class Loaded<T>(val value: T) : Loadable<T>

    /** Удобный хелпер: вернуть значение или null если Loading */
    fun getOrNull(): T? = when (this) {
        is Loading -> null
        is Loaded -> value
    }

    /** Проверка: данные уже загружены (не важно какое значение) */
    val isLoaded: Boolean
        get() = this is Loaded
}