package com.github.bobryanskiy.tamagotchiforlovers.domain.error

sealed interface DomainError

fun DomainError.toUiErrorStringRes(): Int = when (this) {
    is PairError -> this.toUiErrorStringRes()
    is PetError -> this.toUiErrorStringRes()
    is UserError -> this.toUiErrorStringRes()
}