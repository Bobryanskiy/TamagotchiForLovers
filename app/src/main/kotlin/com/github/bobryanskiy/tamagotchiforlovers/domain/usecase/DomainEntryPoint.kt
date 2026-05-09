package com.github.bobryanskiy.tamagotchiforlovers.domain.usecase

sealed interface DomainEntryPoint {
    data object Auth : DomainEntryPoint
    data object Main : DomainEntryPoint
    data class Pet(val petId: String) : DomainEntryPoint
}
