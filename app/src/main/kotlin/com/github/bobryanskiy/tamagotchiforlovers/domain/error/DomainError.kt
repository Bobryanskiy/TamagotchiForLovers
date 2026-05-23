package com.github.bobryanskiy.tamagotchiforlovers.domain.error

sealed interface DomainError

fun DomainError.toUiErrorStringRes(): Int = when (this) {
    is PairError -> toUiErrorStringRes()
    is PetError -> toUiErrorStringRes()
    is UserError -> toUiErrorStringRes()
}