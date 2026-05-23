package com.github.bobryanskiy.tamagotchiforlovers.domain.error

sealed class UserError : DomainError{
    object EmailAlreadyExists : UserError()
    object WeakPassword : UserError()
    object NotAuthenticated : UserError()
    object LoginError : UserError()
}