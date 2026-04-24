package com.github.bobryanskiy.tamagotchiforlovers.domain.error

sealed class PairError : DomainError {
    object GuestOnly : PairError()
    object CreatorOnly : PairError()
    object SessionNotActive : PairError()
    object AlreadyEnded : PairError()
    object InvalidInput : PairError()
    object InvalidRequest : PairError()
    object NotFound : PairError()
    class Network(val cause: Throwable) : PairError()
    object Unknown : PairError()
}