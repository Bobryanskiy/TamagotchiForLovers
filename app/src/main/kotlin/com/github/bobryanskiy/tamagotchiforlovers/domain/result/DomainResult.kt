package com.github.bobryanskiy.tamagotchiforlovers.domain.result

import com.github.bobryanskiy.tamagotchiforlovers.domain.error.DomainError

sealed class DomainResult<out T> {
    data class Success<out T>(val data: T) : DomainResult<T>()
    data class Failure(val error: DomainError) : DomainResult<Nothing>()
}