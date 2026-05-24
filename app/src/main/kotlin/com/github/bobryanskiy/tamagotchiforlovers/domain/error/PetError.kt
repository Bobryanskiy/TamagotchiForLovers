package com.github.bobryanskiy.tamagotchiforlovers.domain.error

sealed class PetError : DomainError {
    object PetNotFound : PetError()
    object ActionBlocked : PetError()
    object ActionWouldKillPet : PetError()
    object NotAuthenticated : PetError()
    object SessionNotFound : PetError()
    object Database : PetError()
    object Network : PetError()
    object Unknown : PetError()
    object PairNotFound : PetError()
    object PairNotActive : PetError()
    object InvalidInput : PetError()
}