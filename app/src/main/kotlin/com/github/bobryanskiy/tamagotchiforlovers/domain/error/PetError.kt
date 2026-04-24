package com.github.bobryanskiy.tamagotchiforlovers.domain.error

sealed class PetError : DomainError {
    data object InvalidInput : PetError()
    object ActionNotAllowed : PetError()
    data object NotFound : PetError()
    data class Network(val cause: Throwable) : PetError()
    data object Unknown : PetError()
}