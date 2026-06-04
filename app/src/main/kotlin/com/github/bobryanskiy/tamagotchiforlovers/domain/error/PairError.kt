package com.github.bobryanskiy.tamagotchiforlovers.domain.error

sealed class PairError : DomainError {
    object GuestOnly : PairError()
    object CreatorOnly : PairError()
    object SessionNotActive : PairError()
    object AlreadyEnded : PairError()
    object PairNotFound : PairError()
    object AlreadyJoined : PairError()
    object InvalidInput : PairError()
    object NotAuthenticated : PairError()
    object InvalidRequest : PairError()
    object Network : PairError()
    object Unknown : PairError()
}
