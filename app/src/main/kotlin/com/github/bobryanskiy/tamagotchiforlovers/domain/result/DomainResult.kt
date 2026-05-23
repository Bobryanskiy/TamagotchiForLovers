package com.github.bobryanskiy.tamagotchiforlovers.domain.result

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.DomainError

sealed class DomainResult<out T, out E : DomainError> {
    inline fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (E) -> R
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Failure -> onFailure(error)
    }

    fun getOrNull(): T? = when(this) { is Success -> data; is Failure -> null }

    data class Success<out T>(val data: T) : DomainResult<T, Nothing>()
    data class Failure<out E: DomainError>(val error: E) : DomainResult<Nothing, E>()
}

inline fun <T, E : DomainError> DomainResult<T, E>.onSuccess(action: (T) -> Unit): DomainResult<T, E> {
    if (this is DomainResult.Success) action(data)
    return this
}

inline fun <T, E : DomainError> DomainResult<T, E>.onFailure(action: (E) -> Unit): DomainResult<T, E> {
    if (this is DomainResult.Failure) action(error)
    return this
}